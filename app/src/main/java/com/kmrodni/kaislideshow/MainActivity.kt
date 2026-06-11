package com.kmrodni.kaislideshow

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
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
    private lateinit var pauseOverlay: LinearLayout

    // State
    private val imageList = mutableListOf<String>()
    private var currentIndex = 0
    private lateinit var settings: SlideshowSettings
    private var isPlaying = false
    private var showUI = true
    private var isLoading = false
    private var isFullscreen = false
    private var initialLoadComplete = false

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
        if (uris != null && uris.isNotEmpty()) {
            handleUris(uris)
        }
    }

    private val pickFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            handleFolderUri(uri)
        }
    }

    // Permission request launcher for Android 13+
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, try the operation again
            showFilePicker()
        } else {
            Toast.makeText(this, "Permission denied. Cannot access files.", Toast.LENGTH_SHORT).show()
        }
    }

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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Handle gestures
        gestureDetector.onTouchEvent(event)
        
        // Also show UI on any touch
        if (!showUI) {
            showUI = true
            updateUIVisibility()
        }
        if (isPlaying) {
            startUiTimer()
        }
        
        return true
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
            // Image loaded successfully
        }

        imageDisplay.setOnImageErrorListener { path ->
            showError(path)
        }

        // Control bar listeners
        controlBar.setOnIntervalChangedListener { newValue ->
            settings = settings.copy(interval = newValue)
            saveSettings()
            if (isPlaying) startSlideTimer()
        }

        controlBar.setOnShuffleChangedListener { newValue ->
            settings = settings.copy(shuffle = newValue)
            saveSettings()
            applySorting()
        }

        controlBar.setOnAddFilesListener {
            pickFiles()
        }

        controlBar.setOnAddFolderListener {
            pickFolder()
        }

        controlBar.setOnExitListener {
            exitApp()
        }
    }

    private fun loadSettings() {
        settings = SlideshowSettings.fromSharedPreferences(prefs)
        controlBar.setInterval(settings.interval)
        controlBar.setShuffle(settings.shuffle)
    }

    private fun saveSettings() {
        settings.saveToSharedPreferences(prefs)
    }

    // File picking methods

    private fun pickFiles() {
        if (isLoading) return
        
        // For Android 13+, we need READ_MEDIA_IMAGES permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            showFilePicker()
        }
    }

    private fun pickFolder() {
        if (isLoading) return
        
        // For Android 13+, we need READ_MEDIA_IMAGES permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            showFolderPicker()
        }
    }

    private fun showFilePicker() {
        isLoading = true
        updateLoadingState()
        
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
            // For Android 13+, use MediaStore
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        pickFilesLauncher.launch(intent)
    }

    private fun showFolderPicker() {
        isLoading = true
        updateLoadingState()
        
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        }
        pickFolderLauncher.launch(intent)
    }

    private fun handleUris(uris: List<Uri>) {
        val imagePaths = mutableListOf<String>()
        
        uris.forEach { uri ->
            // Try to get file path from URI
            val path = FileUtils.getFilePathFromUri(this, uri)
            if (path != null && FileUtils.isSupportedImage(path)) {
                imagePaths.add(path)
            } else {
                // If we can't get path, use URI directly (Glide can handle it)
                imagePaths.add(uri.toString())
            }
        }
        
        isLoading = false
        updateLoadingState()
        
        if (imagePaths.isNotEmpty()) {
            addImages(imagePaths)
        } else {
            Toast.makeText(this, "No valid images found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleFolderUri(treeUri: Uri) {
        // Take persistable URI permission
        contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        
        // Get all images from the selected directory
        val imagePaths = getImagesFromTreeUri(treeUri)
        
        isLoading = false
        updateLoadingState()
        
        if (imagePaths.isNotEmpty()) {
            addImages(imagePaths)
        } else {
            Toast.makeText(this, "No images found in selected folder", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NewApi")
    private fun getImagesFromTreeUri(treeUri: Uri): List<String> {
        val imagePaths = mutableListOf<String>()
        
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val docId = DocumentsContract.getTreeDocumentId(treeUri)
                val split = docId.split(":").toTypedArray()
                if (split.size >= 2) {
                    val type = split[0]
                    val id = split[1]
                    if ("primary".equals(type, ignoreCase = true)) {
                        val basePath = Environment.getExternalStorageDirectory().absolutePath
                        val directory = File("$basePath/$id")
                        if (directory.exists() && directory.isDirectory) {
                            return FileUtils.loadImagesFromDirectory(directory.absolutePath)
                        }
                    }
                }
            }
            emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

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
        
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                // Single file or content URI
                intent.data?.let { uri ->
                    FileUtils.getFilePathFromUri(this, uri)?.let { path ->
                        if (FileUtils.isSupportedImage(path)) {
                            imagePaths.add(path)
                        }
                    } ?: run {
                        // If we can't get the path, use the URI directly
                        imagePaths.add(uri.toString())
                    }
                }
            }
            Intent.ACTION_SEND -> {
                // Single file shared
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { uri ->
                    FileUtils.getFilePathFromUri(this, uri)?.let { path ->
                        if (FileUtils.isSupportedImage(path)) {
                            imagePaths.add(path)
                        }
                    } ?: run {
                        imagePaths.add(uri.toString())
                    }
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                // Multiple files shared
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let { uris ->
                    uris.forEach { uri ->
                        FileUtils.getFilePathFromUri(this, uri)?.let { path ->
                            if (FileUtils.isSupportedImage(path)) {
                                imagePaths.add(path)
                            }
                        } ?: run {
                            imagePaths.add(uri.toString())
                        }
                    }
                }
            }
        }
        
        if (imagePaths.isNotEmpty()) {
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
        togglePlay()
    }

    private fun handleDoubleTap() {
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
            imageDisplay.loadImage(imagePath)
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
        val shouldShowUI = showUI && imageList.isNotEmpty()
        infoBar.visibility = if (shouldShowUI) View.VISIBLE else View.GONE
        controlBar.visibility = if (shouldShowUI) View.VISIBLE else View.GONE
    }

    private fun showError(path: String) {
        errorContainer.visibility = View.VISIBLE
        errorMessage.text = File(path).name
    }
}
