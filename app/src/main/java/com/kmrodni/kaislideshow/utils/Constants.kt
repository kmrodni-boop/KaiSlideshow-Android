package com.kmrodni.kaislideshow.utils

object Constants {
    const val APP_NAME = "Kai Slideshow"
    const val NO_IMAGES_MESSAGE = "No images selected"
    const val NO_IMAGES_SUBTITLE = "Click \"Add Files\" or \"Add Folder\" to get started"
    const val ADD_FILES = "Add Files"
    const val ADD_FOLDER = "Add Folder"
    const val EXIT = "Exit"
    const val INTERVAL_LABEL = "Interval:"
    const val SHUFFLE_LABEL = "Shuffle"
    const val PAUSE_LABEL = "PAUSE"
    const val ERROR_LOADING_IMAGE = "Error loading image"
    const val NO_IMAGES_FOUND = "No images found in the selected folder"
    const val ERROR_ACCESSING_FOLDER = "Error accessing folder"
    
    // UI constants
    const val UI_HIDE_DURATION_MS = 5000L
    const val FADE_ANIMATION_DURATION_MS = 300L
    const val CONTROL_BAR_HEIGHT = 80f
    const val INFO_BAR_PADDING = 20f
    const val BORDER_RADIUS = 20f
    const val BUTTON_BORDER_RADIUS = 10f
    
    // Interval options (in seconds)
    val INTERVAL_OPTIONS = listOf(2, 3, 5, 10, 20, 30, 60, 120)
    
    // Supported image extensions
    val SUPPORTED_IMAGE_EXTENSIONS = listOf(
        ".jpg", ".jpeg", ".png", ".webp", ".bmp", ".gif"
    )
}
