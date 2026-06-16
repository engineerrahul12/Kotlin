package com.rahulsah.studio.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.rahulsah.studio.data.model.LibraryItem
import com.rahulsah.studio.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object MediaStoreHelper {

    // ──────────────────────────────────────────────
    // Load all Studio-downloaded media from MediaStore
    // ──────────────────────────────────────────────

    suspend fun loadStudioMedia(context: Context): List<LibraryItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<LibraryItem>()
        items += loadVideos(context)
        items += loadImages(context)
        items += loadAudio(context)
        items.sortedByDescending { it.createdAt }
    }

    private fun loadVideos(context: Context): List<LibraryItem> {
        val items = mutableListOf<LibraryItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )

        val selection = "${MediaStore.Video.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%/Studio/%")

        context.contentResolver.query(collection, projection, selection, selectionArgs,
            "${MediaStore.Video.Media.DATE_ADDED} DESC")?.use { cursor ->
            val idCol       = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol     = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dataCol     = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val sizeCol     = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val durCol      = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dateCol     = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id       = cursor.getLong(idCol)
                val name     = cursor.getString(nameCol) ?: ""
                val path     = cursor.getString(dataCol) ?: ""
                val size     = cursor.getLong(sizeCol)
                val duration = cursor.getLong(durCol)
                val date     = cursor.getLong(dateCol) * 1000

                val contentUri = Uri.withAppendedPath(collection, id.toString())

                items.add(
                    LibraryItem(
                        id            = id.toString(),
                        title         = name.substringBeforeLast("."),
                        localPath     = contentUri.toString(),
                        thumbnailPath = path, // coil handles video thumb from path
                        mediaType     = MediaType.VIDEO,
                        durationMs    = duration,
                        fileSizeBytes = size,
                        createdAt     = date
                    )
                )
            }
        }
        return items
    }

    private fun loadImages(context: Context): List<LibraryItem> {
        val items = mutableListOf<LibraryItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Images.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%/Studio/%")

        context.contentResolver.query(collection, projection, selection, selectionArgs,
            "${MediaStore.Images.Media.DATE_ADDED} DESC")?.use { cursor ->
            val idCol   = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id   = cursor.getLong(idCol)
                val name = cursor.getString(nameCol) ?: ""
                val path = cursor.getString(dataCol) ?: ""
                val size = cursor.getLong(sizeCol)
                val date = cursor.getLong(dateCol) * 1000

                val contentUri = Uri.withAppendedPath(collection, id.toString())

                items.add(
                    LibraryItem(
                        id            = id.toString(),
                        title         = name.substringBeforeLast("."),
                        localPath     = contentUri.toString(),
                        thumbnailPath = path,
                        mediaType     = MediaType.IMAGE,
                        fileSizeBytes = size,
                        createdAt     = date
                    )
                )
            }
        }
        return items
    }

    private fun loadAudio(context: Context): List<LibraryItem> {
        val items = mutableListOf<LibraryItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%/Studio/%")

        context.contentResolver.query(collection, projection, selection, selectionArgs,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC")?.use { cursor ->
            val idCol   = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val durCol  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id       = cursor.getLong(idCol)
                val name     = cursor.getString(nameCol) ?: ""
                val size     = cursor.getLong(sizeCol)
                val duration = cursor.getLong(durCol)
                val date     = cursor.getLong(dateCol) * 1000

                val contentUri = Uri.withAppendedPath(collection, id.toString())

                items.add(
                    LibraryItem(
                        id            = id.toString(),
                        title         = name.substringBeforeLast("."),
                        localPath     = contentUri.toString(),
                        mediaType     = MediaType.AUDIO,
                        durationMs    = duration,
                        fileSizeBytes = size,
                        createdAt     = date
                    )
                )
            }
        }
        return items
    }

    // ──────────────────────────────────────────────
    // Utility
    // ──────────────────────────────────────────────

    fun formatFileSize(bytes: Long): String = when {
        bytes < 1024         -> "${bytes}B"
        bytes < 1024 * 1024  -> "${"%.1f".format(bytes / 1024f)}KB"
        bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024f * 1024f))}MB"
        else                 -> "${"%.2f".format(bytes / (1024f * 1024f * 1024f))}GB"
    }
}
