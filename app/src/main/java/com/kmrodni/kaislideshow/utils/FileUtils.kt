package com.kmrodni.kaislideshow.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File

/**
 * Utility class for file operations
 */
object FileUtils {
    
    /**
     * Check if a file path has a supported image extension
     */
    fun isSupportedImage(filePath: String): Boolean {
        val lowerPath = filePath.lowercase()
        return Constants.SUPPORTED_IMAGE_EXTENSIONS.any { ext ->
            lowerPath.endsWith(ext)
        }
    }
    
    /**
     * Load image paths from a directory asynchronously
     * Returns a list of valid, existing image file paths
     */
    fun loadImagesFromDirectory(directoryPath: String): List<String> {
        val imagePaths = mutableListOf<String>()
        
        try {
            val dir = File(directoryPath)
            if (!dir.exists() || !dir.isDirectory) {
                return imagePaths
            }
            
            dir.walk()
                .filter { it.isFile }
                .forEach { file ->
                    try {
                        if (isSupportedImage(file.path) && file.exists()) {
                            imagePaths.add(file.path)
                        }
                    } catch (e: Exception) {
                        // Skip files that can't be accessed
                    }
                }
        } catch (e: Exception) {
            // If directory can't be read, return empty list
            return emptyList()
        }
        
        return imagePaths
    }
    
    /**
     * Get unique paths (remove duplicates)
     */
    fun getUniquePaths(paths: List<String>): List<String> {
        return paths.toSet().toList()
    }
    
    /**
     * Get file path from URI
     * Handles both content:// and file:// URIs
     */
    fun getFilePathFromUri(context: Context, uri: Uri): String? {
        return when {
            // File URI
            uri.scheme == "file" -> {
                uri.path
            }
            // Content URI
            uri.scheme == "content" -> {
                getFilePathFromContentUri(context, uri)
            }
            else -> {
                null
            }
        }
    }
    
    /**
     * Get file path from content URI
     * Works for Android 4.4+ (KitKat and above)
     */
    private fun getFilePathFromContentUri(context: Context, uri: Uri): String? {
        return when {
            // For Android 11+ (R) and above
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                getFilePathForAndroid11AndAbove(context, uri)
            }
            // For Android 10 (Q)
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                getFilePathForAndroid10(context, uri)
            }
            // For Android 5-9 (Lollipop to Pie)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                getFilePathForAndroid5To9(context, uri)
            }
            // For Android 4.4 (KitKat)
            else -> {
                getFilePathForAndroid4_4(context, uri)
            }
        }
    }
    
    @Suppress("DEPRECATION")
    private fun getFilePathForAndroid11AndAbove(context: Context, uri: Uri): String? {
        return try {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.RELATIVE_PATH
            )
            
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val displayNameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    val relativePathColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
                    
                    val id = it.getLong(idColumn)
                    val displayName = it.getString(displayNameColumn)
                    val relativePath = it.getString(relativePathColumn)
                    
                    // Try to get the full path
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    
                    // For Android 11+, we need to use the URI directly or copy the file
                    // Return the URI as a string, or try to get a file path
                    if (relativePath != null) {
                        val externalStorageDir = Environment.getExternalStorageDirectory().absolutePath
                        "$externalStorageDir/$relativePath/$displayName"
                    } else {
                        contentUri.toString()
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    @Suppress("DEPRECATION")
    private fun getFilePathForAndroid10(context: Context, uri: Uri): String? {
        return try {
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val id = it.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    getDataColumn(context, contentUri, null, null)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    @Suppress("DEPRECATION")
    private fun getFilePathForAndroid5To9(context: Context, uri: Uri): String? {
        return try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val contentUri = when {
                    split.size >= 2 -> {
                        val type = split[0]
                        val id = split[1]
                        when (type) {
                            "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            else -> MediaStore.Files.getContentUri("external")
                        }
                    }
                    else -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                getDataColumn(context, contentUri, "_id=?", arrayOf(split[1]))
            } else if (uri.authority == "com.android.externalstorage.documents") {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    Environment.getExternalStorageDirectory().absolutePath + "/" + split[1]
                } else {
                    // Handle non-primary storage
                    null
                }
            } else if (uri.authority == "com.android.providers.downloads.documents") {
                val id = DocumentsContract.getDocumentId(uri)
                if (id.startsWith("raw:")) {
                    id.substring(4)
                } else {
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        id.toLong()
                    )
                    getDataColumn(context, contentUri, null, null)
                }
            } else if (uri.authority == "com.android.providers.media.documents") {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                val contentUri = when (type) {
                    "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    else -> MediaStore.Files.getContentUri("external")
                }
                getDataColumn(context, contentUri, "_id=?", arrayOf(split[1]))
            } else {
                getDataColumn(context, uri, null, null)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    @Suppress("DEPRECATION")
    private fun getFilePathForAndroid4_4(context: Context, uri: Uri): String? {
        return try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    Environment.getExternalStorageDirectory().absolutePath + "/" + split[1]
                } else {
                    null
                }
            } else if (uri.authority == "com.android.externalstorage.documents") {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    Environment.getExternalStorageDirectory().absolutePath + "/" + split[1]
                } else {
                    null
                }
            } else {
                getDataColumn(context, uri, null, null)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    @Suppress("DEPRECATION")
    private fun getDataColumn(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        
        return try {
            cursor = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                null
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(column)
                    it.getString(columnIndex)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        } finally {
            cursor?.close()
        }
    }
}
