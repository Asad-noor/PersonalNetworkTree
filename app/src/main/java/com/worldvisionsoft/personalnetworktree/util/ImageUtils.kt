package com.worldvisionsoft.personalnetworktree.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.worldvisionsoft.personalnetworktree.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageUtils {

    private const val TAG = "ImageUtils"

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
            Log.e(TAG, context.getString(R.string.log_error_copying_image), e)
            null
        } finally {
            // Properly close all streams in finally block to ensure cleanup
            inputStream?.let {
                try {
                    it.close()
                } catch (e: Exception) {
                    Log.e(TAG, context.getString(R.string.log_error_closing_input_stream), e)
                }
            }
            outputStream?.let {
                try {
                    it.close()
                } catch (e: Exception) {
                    Log.e(TAG, context.getString(R.string.log_error_closing_output_stream), e)
                }
            }
        }
    }

    /**
     * Deletes a contact photo from internal storage
     */
    fun deleteContactPhoto(context: Context, photoPath: String?): Boolean {
        var file: File? = null

        return try {
            if (photoPath.isNullOrEmpty()) return false

            val uri = Uri.parse(photoPath)
            file = File(uri.path ?: return false)

            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, context.getString(R.string.log_error_deleting_photo), e)
            false
        } finally {
            // Nullify file reference to help with garbage collection
            file = null
        }
    }
}

