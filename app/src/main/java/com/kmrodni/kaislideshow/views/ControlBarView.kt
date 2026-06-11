package com.kmrodni.kaislideshow.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import com.kmrodni.kaislideshow.R

/**
 * Control bar widget with slideshow controls
 * Glassmorphism effect with blur
 */
class ControlBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val addFilesButton: Button
    private val addFolderButton: Button
    private val settingsButton: Button
    private val donateButton: Button
    private val exitButton: Button

    private var onAddFiles: (() -> Unit)? = null
    private var onAddFolder: (() -> Unit)? = null
    private var onSettings: (() -> Unit)? = null
    private var onDonate: (() -> Unit)? = null
    private var onExit: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_control_bar, this, true)
        
        addFilesButton = findViewById(R.id.addFilesButton)
        addFolderButton = findViewById(R.id.addFolderButton)
        settingsButton = findViewById(R.id.settingsButton)
        donateButton = findViewById(R.id.donateButton)
        exitButton = findViewById(R.id.exitButton)

        addFilesButton.setOnClickListener { onAddFiles?.invoke() }
        addFolderButton.setOnClickListener { onAddFolder?.invoke() }
        settingsButton.setOnClickListener { onSettings?.invoke() }
        donateButton.setOnClickListener { onDonate?.invoke() }
        exitButton.setOnClickListener { onExit?.invoke() }
    }

    /**
     * Set the current interval value - Removed as it moved to settings dialog
     */
    fun setInterval(interval: Int) {}

    /**
     * Set the shuffle state - Removed as it moved to settings dialog
     */
    fun setShuffle(shuffle: Boolean) {}

    /**
     * Set the playing state (enable/disable controls)
     */
    fun setPlaying(isPlaying: Boolean) {
        // All controls can remain enabled
    }

    // Setters for listeners
    fun setOnIntervalChangedListener(listener: (Int) -> Unit) {}

    fun setOnShuffleChangedListener(listener: (Boolean) -> Unit) {}

    fun setOnAddFilesListener(listener: () -> Unit) {
        onAddFiles = listener
    }

    fun setOnAddFolderListener(listener: () -> Unit) {
        onAddFolder = listener
    }

    fun setOnSettingsListener(listener: () -> Unit) {
        onSettings = listener
    }

    fun setOnDonateListener(listener: () -> Unit) {
        onDonate = listener
    }

    fun setOnExitListener(listener: () -> Unit) {
        onExit = listener
    }
}
