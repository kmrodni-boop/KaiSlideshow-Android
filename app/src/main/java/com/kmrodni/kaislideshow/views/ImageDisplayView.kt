package com.kmrodni.kaislideshow.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.kmrodni.kaislideshow.utils.FileUtils
import java.io.File

/**
 * Custom ImageView for displaying images with proper aspect ratio
 * Supports both file:// paths and content:// URIs (Android)
 */
class ImageDisplayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    private var currentImagePath: String? = null
    private var onImageLoaded: (() -> Unit)? = null
    private var onImageError: ((String) -> Unit)? = null

    init {
        scaleType = ScaleType.CENTER_INSIDE
        adjustViewBounds = true
    }

    /**
     * Load an image from a file path or URI
     */
    fun loadImage(imagePath: String) {
        currentImagePath = imagePath
        
        // Clear previous image
        setImageDrawable(null)
        
        try {
            // Handle content:// URIs
            if (imagePath.startsWith("content://")) {
                loadFromUri(Uri.parse(imagePath))
                return
            }
            
            // Handle file:// URIs
            if (imagePath.startsWith("file://")) {
                val path = imagePath.substring(7) // Remove 'file://' prefix
                loadFromFile(File(path))
                return
            }
            
            // Handle regular file paths
            val file = File(imagePath)
            if (file.exists()) {
                loadFromFile(file)
            } else {
                onImageError?.invoke(imagePath)
            }
        } catch (e: Exception) {
            onImageError?.invoke(imagePath)
        }
    }

    private fun loadFromFile(file: File) {
        Glide.with(context)
            .load(file)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    onImageError?.invoke(currentImagePath ?: "")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    onImageLoaded?.invoke()
                    return false
                }
            })
            .into(this)
    }

    private fun loadFromUri(uri: Uri) {
        Glide.with(context)
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    onImageError?.invoke(currentImagePath ?: "")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    onImageLoaded?.invoke()
                    return false
                }
            })
            .into(this)
    }

    fun setOnImageLoadedListener(listener: () -> Unit) {
        onImageLoaded = listener
    }

    fun setOnImageErrorListener(listener: (String) -> Unit) {
        onImageError = listener
    }

    fun clearImage() {
        Glide.with(context).clear(this)
        setImageDrawable(null)
        currentImagePath = null
    }
}
