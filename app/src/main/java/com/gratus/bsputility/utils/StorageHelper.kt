package com.gratus.bsputility.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object StorageHelper {

    data class ExportResult(
        val success: Boolean,
        val filePathOrUri: String = "",
        val errorMessage: String? = null
    )

    fun exportToDocuments(
        context: Context,
        fileName: String,
        mimeType: String,
        content: String
    ): ExportResult {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/BSPManpower")
                }
                val resolver = context.contentResolver
                val uri: Uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                    ?: return exportToAppSpecificDocuments(context, fileName, content)

                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content.toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                }
                ExportResult(success = true, filePathOrUri = "Documents/BSPManpower/$fileName")
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "BSPManpower")
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val file = File(dir, fileName)
                FileOutputStream(file).use { fos ->
                    fos.write(content.toByteArray(Charsets.UTF_8))
                    fos.flush()
                }
                ExportResult(success = true, filePathOrUri = file.absolutePath)
            }
        } catch (e: Exception) {
            exportToAppSpecificDocuments(context, fileName, content)
        }
    }

    private fun exportToAppSpecificDocuments(context: Context, fileName: String, content: String): ExportResult {
        return try {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "BSPManpower")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, fileName)
            FileOutputStream(file).use { fos ->
                fos.write(content.toByteArray(Charsets.UTF_8))
                fos.flush()
            }
            ExportResult(success = true, filePathOrUri = file.absolutePath)
        } catch (e: Exception) {
            ExportResult(success = false, errorMessage = e.localizedMessage ?: "Failed to write file")
        }
    }

    fun readTextFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                it.readText()
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        name = cursor.getString(index)
                    }
                }
            }
        } catch (_: Exception) {}
        return name ?: uri.lastPathSegment
    }
}
