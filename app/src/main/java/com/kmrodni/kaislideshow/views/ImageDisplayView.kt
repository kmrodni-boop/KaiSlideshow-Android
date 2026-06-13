package com.kmrodni.kaislideshow.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.kmrodni.kaislideshow.models.SlideshowSettings
import java.io.File

/**
 * Custom ImageView for displaying images with proper aspect ratio
 * Supports both file:// paths and content:// URIs (Android)
 * Handles transitions, Ken Burns effect, manual zoom steps, and panning
 */
class ImageDisplayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var currentImagePath: String? = null
    private var onImageLoaded: (() -> Unit)? = null
    private var onImageError: ((String) -> Unit)? = null

    // Zoom and Pan state
    private var currentScale = 1.0f
    private val zoomSteps = listOf(1.0f, 1.5f, 2.0f, 3.0f, 4.0f)
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isPanning = false

    init {
        scaleType = ScaleType.CENTER_INSIDE
        adjustViewBounds = true
    }

    /**
     * Load an image from a file path or URI with specific settings
     */
    fun loadImage(imagePath: String, settings: SlideshowSettings, startKenBurns: Boolean) {
        currentImagePath = imagePath
        
        // Reset everything for new image
        resetTransformations()
        
        var builder = Glide.with(context)
            .asDrawable()
            .load(if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) Uri.parse(imagePath) else File(imagePath))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)

        if (settings.transitionEnabled) {
            builder = builder.transition(DrawableTransitionOptions.withCrossFade(800)) // 0.8s fade
        }

        builder.listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                onImageError?.invoke(currentImagePath ?: "")
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                onImageLoaded?.invoke()
                if (startKenBurns && settings.kenBurnsEnabled) {
                    startKenBurnsEffect(settings.interval)
                }
                return false
            }
        }).into(this)
    }

    /**
     * Increment zoom level
     */
    fun zoomIn() {
        val nextStep = zoomSteps.firstOrNull { it > currentScale } ?: zoomSteps.last()
        applyScale(nextStep)
    }

    /**
     * Decrement zoom level
     */
    fun zoomOut() {
        val prevStep = zoomSteps.lastOrNull { it < currentScale } ?: zoomSteps.first()
        applyScale(prevStep)
    }

    private fun applyScale(newScale: Float) {
        animate().cancel()
        currentScale = newScale
        
        // Smoothly animate to new scale
        animate()
            .scaleX(currentScale)
            .scaleY(currentScale)
            .setDuration(300)
            .withEndAction {
                if (currentScale <= 1.0f) {
                    // Reset translation when back to 1x
                    animate().translationX(0f).translationY(0f).setDuration(200).start()
                }
            }
            .start()
    }

    /**
     * Reset all zoom and pan transformations
     */
    fun resetTransformations() {
        animate().cancel()
        currentScale = 1.0f
        scaleX = 1.0f
        scaleY = 1.0f
        translationX = 0f
        translationY = 0f
    }

    /**
     * Check if the image is currently zoomed in
     */
    fun isZoomed(): Boolean {
        return currentScale > 1.0f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (currentScale <= 1.0f) return super.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.rawX
                lastTouchY = event.rawY
                isPanning = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isPanning) {
                    val dx = event.rawX - lastTouchX
                    val dy = event.rawY - lastTouchY
                    
                    translationX += dx
                    translationY += dy
                    
                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isPanning = false
            }
        }
        return true
    }

    private fun startKenBurnsEffect(intervalSeconds: Int) {
        animate().cancel()
        scaleX = 1.0f
        scaleY = 1.0f
        
        val scale = 1.08f + (Math.random() * 0.05f).toFloat()
        animate()
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(intervalSeconds * 1000L + 2000L)
            .setInterpolator(LinearInterpolator())
            .start()
    }

    fun setOnImageLoadedListener(listener: () -> Unit) {
        onImageLoaded = listener
    }

    fun setOnImageErrorListener(listener: (String) -> Unit) {
        onImageError = listener
    }

    fun clearImage() {
        resetTransformations()
        Glide.with(context).clear(this)
        setImageDrawable(null)
        currentImagePath = null
    }
}
