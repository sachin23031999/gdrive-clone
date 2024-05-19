package com.sachin.gdrive.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.sachin.gdrive.model.DriveEntity
import com.sachin.gdrive.provider.DriveServiceProvider
import java.io.IOException
import java.io.InputStream


class DriveRepository(
    private val driveService: DriveServiceProvider
) {

    fun initialise(context: Context): Boolean =
        driveService.createService(context)


    suspend fun uploadFile(
        context: Context,
        fileName: String,
        fileUri: Uri,
        parentFolderId: String,
        callback: (Int, String?) -> Unit
    ) {
        try {
            val contentResolver: ContentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(fileUri)
            inputStream?.let { stream ->
                driveService.write(
                    context, fileName, stream, parentFolderId
                ) { progress, error ->
                    callback(progress, error)
                }
                stream.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    suspend fun createFolder(parent: String, name: String): String? =
        driveService.createFolder(parent, name)


    suspend fun queryAllItems(context: Context, parent: String): List<DriveEntity> =
        driveService.queryAll(context, parent) ?: emptyList()

}