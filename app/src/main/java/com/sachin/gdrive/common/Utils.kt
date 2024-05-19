package com.sachin.gdrive.common

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object Utils {

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