package com.kmrodni.kaislideshow

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.documentfile.provider.DocumentFile
import com.kmrodni.kaislideshow.databinding.ActivityMainBinding
import com.kmrodni.kaislideshow.models.SlideshowSettings
import com.kmrodni.kaislideshow.utils.Constants
import com.kmrodni.kaislideshow.utils.FileUtils
import com.kmrodni.kaislideshow.views.ControlBarView
import com.kmrodni.kaislideshow.views.ImageDisplayView
import com.kmrodni.kaislideshow.views.InfoBarView
import java.io.File
import java.util.Random

/**
 * Main activity for the Kai Slideshow app
 * Handles image slideshow with various controls and gestures
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageDisplay: ImageDisplayView
    private lateinit var infoBar: InfoBarView
    private lateinit var controlBar: ControlBarView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var errorContainer: LinearLayout
    private lateinit var errorMessage: TextView
    private lateinit var emptyState: LinearLayout
    private lateinit var pauseOverlay: android.widget.FrameLayout

    // State
    private val imageList = mutableListOf<String>()
    private var currentIndex = 0
    private lateinit var settings: SlideshowSettings
    private var isPlaying = false
    private var showUI = true
    private var isLoading = false
    private var isFullscreen = false
    private var sleepTimerHandler = Handler(Looper.getMainLooper())
    private var sleepTimerRunnable: Runnable? = null

    // Timers
    private val slideHandler = Handler(Looper.getMainLooper())
    private val uiHandler = Handler(Looper.getMainLooper())
    private var slideRunnable: Runnable? = null
    private var uiRunnable: Runnable? = null

    // Gesture detection
    private lateinit var gestureDetector: GestureDetectorCompat

    // Preferences
    private val prefs by lazy {
        getSharedPreferences("KaiSlideshowPrefs", MODE_PRIVATE)
    }

    // Activity result launchers for file/folder picking
    private val pickFilesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            handleUris(uris)
        } else {
            isLoading = false
            updateLoadingState()
        }
    }

    private val pickFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            handleFolderUri(uri)
        } else {
            isLoading = false
            updateLoadingState()
        }
    }

    // Permission request launcher
    // Removed as it's not needed for basic file picking anymore

    // Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set fullscreen mode
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views
        initializeViews()
        
        // Initialize gesture detector
        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                handleTap()
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                handleDoubleTap()
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                handleLongPress()
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 != null) {
                    val diffX = e2.x - e1.x
                    if (diffX > 100) {
                        // Swipe right - previous image
                        handleSwipeRight()
                    } else if (diffX < -100) {
                        // Swipe left - next image
                        handleSwipeLeft()
                    }
                }
                return true
            }
        })

        // Load settings
        loadSettings()
        
        // Process initial intent (images shared with the app)
        processInitialIntent(intent)
        
        // Set up view click listeners
        setupViewListeners()
        
        // Start UI hide timer
        startUiTimer()

        // Initial UI state update
        updateUIState()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        processInitialIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up handlers
        slideHandler.removeCallbacksAndMessages(null)
        uiHandler.removeCallbacksAndMessages(null)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // Find which view is under the touch
        val viewUnderTouch = binding.root.findChildViewUnder(event.x, event.y)
        
        // If we are touching the control bar, don't let the gesture detector handle it
        // and don't trigger the UI hide timer reset from here
        if (controlBar.visibility == View.VISIBLE && isPointInsideView(event.rawX, event.rawY, controlBar)) {
            return super.dispatchTouchEvent(event)
        }

        gestureDetector.onTouchEvent(event)
        
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (!showUI) {
                showUI = true
                updateUIVisibility()
            }
            if (isPlaying) {
                startUiTimer()
            }
        }
        
        return super.dispatchTouchEvent(event)
    }

    private fun isPointInsideView(x: Float, y: Float, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val viewX = location[0]
        val viewY = location[1]

        return (x > viewX && x < (viewX + view.width)) &&
                (y > viewY && y < (viewY + view.height))
    }

    private fun android.view.ViewGroup.findChildViewUnder(x: Float, y: Float): View? {
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i)
            if (x >= child.left && x < child.right && y >= child.top && y < child.bottom) {
                return child
            }
        }
        return null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    // Initialization

    private fun initializeViews() {
        imageDisplay = binding.imageDisplay
        infoBar = binding.infoBar
        controlBar = binding.controlBar
        loadingIndicator = binding.loadingIndicator
        errorContainer = binding.errorContainer
        errorMessage = binding.errorMessage
        emptyState = binding.emptyState
        pauseOverlay = binding.pauseOverlay
    }

    private fun setupViewListeners() {
        // Image display listeners
        imageDisplay.setOnImageLoadedListener {
            errorContainer.visibility = View.GONE
        }

        imageDisplay.setOnImageErrorListener { path ->
            showError(path)
        }

        // Control bar listeners
        controlBar.setOnAddFilesListener {
            pickFiles()
        }

        controlBar.setOnAddFolderListener {
            pickFolder()
        }

        controlBar.setOnSettingsListener {
            showSettingsDialog()
        }

        controlBar.setOnDonateListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://paypal.me/kaimarius"))
            startActivity(intent)
        }

        controlBar.setOnExitListener {
            exitApp()
        }

        // Empty state listeners
        binding.emptyAddFiles.setOnClickListener {
            pickFiles()
        }

        binding.emptyAddFolder.setOnClickListener {
            pickFolder()
        }
    }

    private fun loadSettings() {
        settings = SlideshowSettings.fromSharedPreferences(prefs)
        applySettings()
    }

    private fun saveSettings() {
        settings.saveToSharedPreferences(prefs)
        applySettings()
    }

    private fun applySettings() {
        // Re-start timers if playing
        if (isPlaying) {
            startSlideTimer()
        }
        
        // Handle sleep timer
        startSleepTimer()
        
        // Apply shuffle if needed
        applySorting()
    }

    private fun showSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)
        val intervalSpinner = dialogView.findViewById<Spinner>(R.id.settingsIntervalSpinner)
        val shuffleCheckbox = dialogView.findViewById<CheckBox>(R.id.settingsShuffleCheckbox)
        val transitionCheckbox = dialogView.findViewById<CheckBox>(R.id.settingsTransitionCheckbox)
        val kenBurnsCheckbox = dialogView.findViewById<CheckBox>(R.id.settingsKenBurnsCheckbox)
        val sleepTimerSpinner = dialogView.findViewById<Spinner>(R.id.settingsSleepTimerSpinner)

        // Setup interval spinner
        val intervalAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Constants.INTERVAL_OPTIONS)
        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        intervalSpinner.adapter = intervalAdapter
        intervalSpinner.setSelection(Constants.INTERVAL_OPTIONS.indexOf(settings.interval))

        // Setup sleep timer spinner
        val sleepTimerOptions = listOf(0, 15, 30, 60, 120, 240)
        val sleepTimerLabels = listOf(
            getString(R.string.off),
            getString(R.string.minutes_15),
            getString(R.string.minutes_30),
            getString(R.string.hour_1),
            getString(R.string.hours_2),
            getString(R.string.hours_4)
        )
        val sleepTimerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sleepTimerLabels)
        sleepTimerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sleepTimerSpinner.adapter = sleepTimerAdapter
        sleepTimerSpinner.setSelection(sleepTimerOptions.indexOf(settings.sleepTimerMinutes))

        // Set current values
        shuffleCheckbox.isChecked = settings.shuffle
        transitionCheckbox.isChecked = settings.transitionEnabled
        kenBurnsCheckbox.isChecked = settings.kenBurnsEnabled

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val newInterval = Constants.INTERVAL_OPTIONS[intervalSpinner.selectedItemPosition]
                val newSleepTimer = sleepTimerOptions[sleepTimerSpinner.selectedItemPosition]
                
                settings = settings.copy(
                    interval = newInterval,
                    shuffle = shuffleCheckbox.isChecked,
                    transitionEnabled = transitionCheckbox.isChecked,
                    kenBurnsEnabled = kenBurnsCheckbox.isChecked,
                    sleepTimerMinutes = newSleepTimer
                )
                saveSettings()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun startSleepTimer() {
        sleepTimerRunnable?.let { sleepTimerHandler.removeCallbacks(it) }
        
        if (settings.sleepTimerMinutes > 0) {
            sleepTimerRunnable = Runnable {
                exitApp()
            }
            sleepTimerHandler.postDelayed(sleepTimerRunnable!!, settings.sleepTimerMinutes * 60 * 1000L)
        }
    }

    // File picking methods

    private fun pickFiles() {
        if (isLoading) return
        showFilePicker()
    }

    private fun pickFolder() {
        if (isLoading) return
        showFolderPicker()
    }

    private fun showFilePicker() {
        isLoading = true
        updateLoadingState()
        pickFilesLauncher.launch("image/*")
    }

    private fun showFolderPicker() {
        isLoading = true
        updateLoadingState()
        pickFolderLauncher.launch(null)
    }

    private fun handleUris(uris: List<Uri>) {
        val imagePaths = mutableListOf<String>()
        
        uris.forEach { uri ->
            imagePaths.add(uri.toString())
        }
        
        isLoading = false
        updateLoadingState()
        
        if (imagePaths.isNotEmpty()) {
            addImages(imagePaths)
        } else {
            Toast.makeText(this, R.string.no_images_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleFolderUri(treeUri: Uri) {
        isLoading = true
        updateLoadingState()
        
        // Take persistable URI permission
        contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        
        // Use a background thread or Coroutine for large folder traversal
        // For now, doing it simply
        val imagePaths = mutableListOf<String>()
        val root = DocumentFile.fromTreeUri(this, treeUri)
        root?.listFiles()?.forEach { file ->
            if (file.isFile && file.type?.startsWith("image/") == true) {
                imagePaths.add(file.uri.toString())
            }
        }
        
        isLoading = false
        updateLoadingState()
        
        if (imagePaths.isNotEmpty()) {
            addImages(imagePaths)
        } else {
            Toast.makeText(this, R.string.no_images_found, Toast.LENGTH_SHORT).show()
        }
    }

    // Removed getImagesFromTreeUri as it's replaced by handleFolderUri logic

    // Intent handling

    private fun processInitialIntent(intent: Intent?) {
        intent?.let { 
            if (intent.action == Intent.ACTION_VIEW || 
                intent.action == Intent.ACTION_SEND ||
                intent.action == Intent.ACTION_SEND_MULTIPLE) {
                handleIncomingIntent(intent)
            }
        }
    }

    private fun handleIncomingIntent(intent: Intent) {
        val imagePaths = mutableListOf<String>()
        
        try {
            when (intent.action) {
                Intent.ACTION_VIEW -> {
                    intent.data?.let { uri ->
                        imagePaths.add(uri.toString())
                    }
                }
                Intent.ACTION_SEND -> {
                    intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { uri ->
                        imagePaths.add(uri.toString())
                    }
                }
                Intent.ACTION_SEND_MULTIPLE -> {
                    intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let { uris ->
                        uris.forEach { uri ->
                            imagePaths.add(uri.toString())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        if (imagePaths.isNotEmpty()) {
            // If we are getting images shared, we should probably show them and NOT auto-play immediately
            // or at least make sure the UI is visible for a moment.
            showUI = true
            addImages(imagePaths)
        }
    }

    // Image management

    private fun addImages(newImagePaths: List<String>) {
        val uniquePaths = FileUtils.getUniquePaths(newImagePaths)
        imageList.addAll(uniquePaths)
        applySorting()
        
        if (!isPlaying && imageList.isNotEmpty()) {
            togglePlay()
        }
        
        updateUIState()
    }

    private fun applySorting() {
        currentIndex = 0
        if (settings.shuffle) {
            imageList.shuffle(Random())
        } else {
            imageList.sort()
        }
        updateUIState()
    }

    // Navigation

    private fun showNext() {
        if (imageList.isEmpty()) return
        currentIndex = (currentIndex + 1) % imageList.size
        updateUIState()
    }

    private fun showPrevious() {
        if (imageList.isEmpty()) return
        currentIndex = (currentIndex - 1) % imageList.size
        if (currentIndex < 0) currentIndex = imageList.size - 1
        updateUIState()
    }

    // Playback control

    private fun togglePlay() {
        if (isPlaying) {
            // Stop slideshow
            slideRunnable?.let { slideHandler.removeCallbacks(it) }
            isPlaying = false
            showUI = true
            uiRunnable?.let { uiHandler.removeCallbacks(it) }
        } else {
            // Start slideshow
            isPlaying = true
            showUI = false
            startSlideTimer()
            startUiTimer()
        }
        updateUIState()
    }

    private fun startSlideTimer() {
        slideRunnable?.let { slideHandler.removeCallbacks(it) }
        if (imageList.isNotEmpty()) {
            slideRunnable = object : Runnable {
                override fun run() {
                    if (imageList.isNotEmpty()) {
                        showNext()
                        slideHandler.postDelayed(this, (settings.interval * 1000).toLong())
                    }
                }
            }
            slideHandler.postDelayed(slideRunnable!!, (settings.interval * 1000).toLong())
        }
    }

    private fun startUiTimer() {
        uiRunnable?.let { uiHandler.removeCallbacks(it) }
        if (!isPlaying) return
        
        uiRunnable = object : Runnable {
            override fun run() {
                if (isPlaying) {
                    showUI = false
                    updateUIVisibility()
                }
            }
        }
        uiHandler.postDelayed(uiRunnable!!, Constants.UI_HIDE_DURATION_MS)
    }

    // Fullscreen control

    private fun toggleFullscreen() {
        try {
            if (isFullscreen) {
                // Exit fullscreen
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                supportActionBar?.show()
            } else {
                // Enter fullscreen
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
                supportActionBar?.hide()
            }
            isFullscreen = !isFullscreen
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Exit

    private fun exitApp() {
        if (isPlaying) {
            togglePlay()
            // Small delay to show UI before exiting
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 300)
        } else {
            finish()
        }
    }

    // Gesture handling

    private fun handleTap() {
        if (imageList.isEmpty()) return
        // Just show UI, don't toggle play
        showUI = true
        updateUIVisibility()
        if (isPlaying) {
            startUiTimer()
        }
    }

    private fun handleDoubleTap() {
        if (imageList.isEmpty()) return
        togglePlay()
    }

    private fun handleLongPress() {
        if (imageList.isEmpty()) return
        toggleFullscreen()
    }

    private fun handleSwipeLeft() {
        if (imageList.isEmpty()) return
        showNext()
        startUiTimer()
    }

    private fun handleSwipeRight() {
        if (imageList.isEmpty()) return
        showPrevious()
        startUiTimer()
    }

    // UI updates

    private fun updateUIState() {
        updateImageDisplay()
        updateInfoBar()
        updateControlBar()
        updateEmptyState()
        updatePauseOverlay()
        updateUIVisibility()
    }

    private fun updateImageDisplay() {
        if (imageList.isNotEmpty() && currentIndex < imageList.size) {
            val imagePath = imageList[currentIndex]
            imageDisplay.loadImage(imagePath, settings)
        } else {
            imageDisplay.clearImage()
        }
    }

    private fun updateInfoBar() {
        if (imageList.isNotEmpty() && currentIndex < imageList.size) {
            infoBar.updateInfo(currentIndex + 1, imageList.size, imageList[currentIndex])
        }
    }

    private fun updateControlBar() {
        controlBar.setPlaying(isPlaying)
    }

    private fun updateEmptyState() {
        emptyState.visibility = if (imageList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updatePauseOverlay() {
        pauseOverlay.visibility = if (!isPlaying && imageList.isNotEmpty() && showUI) View.VISIBLE else View.GONE
    }

    private fun updateLoadingState() {
        loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun updateUIVisibility() {
        val shouldShowUI = showUI || imageList.isEmpty()
        infoBar.visibility = if (shouldShowUI && imageList.isNotEmpty()) View.VISIBLE else View.GONE
        controlBar.visibility = if (shouldShowUI) View.VISIBLE else View.GONE
    }

    private fun showError(path: String) {
        errorContainer.visibility = View.VISIBLE
        errorMessage.text = File(path).name
    }
}
