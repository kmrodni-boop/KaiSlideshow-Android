package com.kmrodni.kaislideshow.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.kmrodni.kaislideshow.R
import com.kmrodni.kaislideshow.utils.Constants

/**
 * Control bar widget with slideshow controls
 * Glassmorphism effect with blur
 */
class ControlBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val intervalSpinner: Spinner
    private val shuffleCheckbox: CheckBox
    private val addFilesButton: Button
    private val addFolderButton: Button
    private val exitButton: Button

    private var onIntervalChanged: ((Int) -> Unit)? = null
    private var onShuffleChanged: ((Boolean) -> Unit)? = null
    private var onAddFiles: (() -> Unit)? = null
    private var onAddFolder: (() -> Unit)? = null
    private var onExit: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_control_bar, this, true)
        
        intervalSpinner = findViewById(R.id.intervalSpinner)
        shuffleCheckbox = findViewById(R.id.shuffleCheckbox)
        addFilesButton = findViewById(R.id.addFilesButton)
        addFolderButton = findViewById(R.id.addFolderButton)
        exitButton = findViewById(R.id.exitButton)

        // Setup interval spinner
        val intervalAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            Constants.INTERVAL_OPTIONS
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        intervalSpinner.adapter = intervalAdapter

        // Set listeners
        intervalSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedValue = Constants.INTERVAL_OPTIONS[position]
                onIntervalChanged?.invoke(selectedValue)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                // Do nothing
            }
        }

        shuffleCheckbox.setOnCheckedChangeListener { _, isChecked ->
            onShuffleChanged?.invoke(isChecked)
        }

        addFilesButton.setOnClickListener { onAddFiles?.invoke() }
        addFolderButton.setOnClickListener { onAddFolder?.invoke() }
        exitButton.setOnClickListener { onExit?.invoke() }
    }

    /**
     * Set the current interval value
     */
    fun setInterval(interval: Int) {
        val position = Constants.INTERVAL_OPTIONS.indexOf(interval)
        if (position >= 0) {
            intervalSpinner.setSelection(position)
        }
    }

    /**
     * Set the shuffle state
     */
    fun setShuffle(shuffle: Boolean) {
        shuffleCheckbox.isChecked = shuffle
    }

    /**
     * Set the playing state (enable/disable controls)
     */
    fun setPlaying(isPlaying: Boolean) {
        intervalSpinner.isEnabled = !isPlaying
        shuffleCheckbox.isEnabled = !isPlaying
    }

    // Setters for listeners
    fun setOnIntervalChangedListener(listener: (Int) -> Unit) {
        onIntervalChanged = listener
    }

    fun setOnShuffleChangedListener(listener: (Boolean) -> Unit) {
        onShuffleChanged = listener
    }

    fun setOnAddFilesListener(listener: () -> Unit) {
        onAddFiles = listener
    }

    fun setOnAddFolderListener(listener: () -> Unit) {
        onAddFolder = listener
    }

    fun setOnExitListener(listener: () -> Unit) {
        onExit = listener
    }
}
