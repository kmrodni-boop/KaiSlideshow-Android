package com.kmrodni.kaislideshow.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
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
 * Handles transitions and Ken Burns effect
 */
class ImageDisplayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var currentImagePath: String? = null
    private var onImageLoaded: (() -> Unit)? = null
    private var onImageError: ((String) -> Unit)? = null

    init {
        scaleType = ScaleType.CENTER_CROP // CROP looks better for Ken Burns
        adjustViewBounds = true
    }

    /**
     * Load an image from a file path or URI with specific settings
     */
    fun loadImage(imagePath: String, settings: SlideshowSettings) {
        currentImagePath = imagePath
        
        // Reset animations
        animate().cancel()
        scaleX = 1.0f
        scaleY = 1.0f
        translationX = 0f
        translationY = 0f
        
        var builder = Glide.with(context)
            .asDrawable()
            .load(if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) Uri.parse(imagePath) else File(imagePath))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)

        if (settings.transitionEnabled) {
            builder = builder.transition(DrawableTransitionOptions.withCrossFade(800)) // Slower fade (0.8s)
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
                if (settings.kenBurnsEnabled) {
                    startKenBurnsEffect(settings.interval)
                }
                return false
            }
        }).into(this)
    }

    private fun startKenBurnsEffect(intervalSeconds: Int) {
        // Randomly choose a scale and translation direction
        val scale = 1.08f + (Math.random() * 0.05f).toFloat() // Reduced scale for slower feel
        
        animate()
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(intervalSeconds * 1000L + 2000L) // Longer duration for smoother/slower movement
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
        animate().cancel()
        Glide.with(context).clear(this)
        setImageDrawable(null)
        currentImagePath = null
    }
}
