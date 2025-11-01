package com.worldvisionsoft.personalnetworktree.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageUtils {
    /**
     * Copies an image from a content URI to app's internal storage
     * This ensures the image is always accessible, even after app restart
     */
    fun copyImageToInternalStorage(context: Context, sourceUri: Uri, contactId: String): String? {
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        return try {
            // Create a unique filename for the contact photo
            val fileName = "contact_${contactId}_${UUID.randomUUID()}.jpg"

            // Get the app's internal storage directory for contact photos
            val photosDir = File(context.filesDir, "contact_photos")
            if (!photosDir.exists()) {
                photosDir.mkdirs()
            }

            // Create the destination file
            val destinationFile = File(photosDir, fileName)

            // Copy the image from source URI to internal storage
            inputStream = context.contentResolver.openInputStream(sourceUri)
            outputStream = FileOutputStream(destinationFile)

            inputStream?.copyTo(outputStream)

            // Return the file URI as a string
            Uri.fromFile(destinationFile).toString()
        } catch (e: Exception) {
            android.util.Log.e("ImageUtils", "Error copying image", e)
            null
        } finally {
            try {
                inputStream?.close()
            } catch (e: Exception) {
                android.util.Log.e("ImageUtils", "Error closing input stream", e)
            }
            try {
                outputStream?.close()
            } catch (e: Exception) {
                android.util.Log.e("ImageUtils", "Error closing output stream", e)
            }
        }
    }

    /**
     * Deletes a contact photo from internal storage
     */
    fun deleteContactPhoto(context: Context, photoPath: String?): Boolean {
        return try {
            if (photoPath.isNullOrEmpty()) return false

            val uri = Uri.parse(photoPath)
            val file = File(uri.path ?: return false)

            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("ImageUtils", "Error deleting photo", e)
            false
        }
    }
}

