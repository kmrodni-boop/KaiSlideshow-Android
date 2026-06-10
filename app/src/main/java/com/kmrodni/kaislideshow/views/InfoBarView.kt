package com.kmrodni.kaislideshow.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.kmrodni.kaislideshow.R
import java.io.File

/**
 * Info bar widget showing current image index and filename
 * Positioned at the bottom-left corner
 */
class InfoBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val infoTextView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_info_bar, this, true)
        infoTextView = findViewById(R.id.infoText)
        
        // Set background and styling
        setBackgroundResource(R.drawable.info_bar_bg)
        orientation = HORIZONTAL
        setPadding(32, 16, 32, 16)
    }

    /**
     * Update the info bar with current image information
     */
    fun updateInfo(currentIndex: Int, totalImages: Int, imagePath: String) {
        val filename = File(imagePath).name
        infoTextView.text = "$currentIndex / $totalImages   •   $filename"
    }
}
