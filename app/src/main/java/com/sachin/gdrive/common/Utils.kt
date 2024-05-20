package com.sachin.gdrive.common

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Utils {

    /**
     * Util method to get the file name from its URI.
     */
    @SuppressLint("Range")
    fun getFilename(context: Context, uri: Uri?): String {
        if (uri == null) return ""

        var filename = ""
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                filename = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }

        return filename
    }
}
