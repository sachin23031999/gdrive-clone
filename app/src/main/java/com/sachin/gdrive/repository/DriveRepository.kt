package com.sachin.gdrive.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.work.Data
import androidx.work.WorkManager
import com.sachin.gdrive.common.log.logD
import com.sachin.gdrive.model.DriveEntity
import com.sachin.gdrive.provider.DriveServiceProvider
import com.sachin.gdrive.worker.FileUploadWorker
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * Repository for drive related operations.
 */
class DriveRepository(
    private val driveService: DriveServiceProvider
) {
    val workRequestIds = mutableListOf<UUID>()
    fun initialise(context: Context): Boolean =
        driveService.createService(context)


    fun uploadFile(
        context: Context,
        fileName: String,
        fileUri: Uri,
        parentFolderId: String,
        callback: (fileName: String, progress: Int, error: String?) -> Unit
    ) {
        logD { "upload file: $fileName" }
        val inputData = Data.Builder()
            .putString(FileUploadWorker.EXTRA_FILE_NAME, fileName)
            .putString(FileUploadWorker.EXTRA_FILE_URI, fileUri.toString())
            .putString(FileUploadWorker.EXTRA_PARENT_FOLDER_ID, parentFolderId)
            .build()

        val currentWorkId = FileUploadWorker.enqueue(context, inputData)
        workRequestIds.add(currentWorkId)
        workRequestIds.forEach { id ->
            WorkManager.getInstance(context).getWorkInfoByIdLiveData(id)
                .observeForever { info ->
                    info?.let {
                        val progress = it.progress.getInt(FileUploadWorker.EXTRA_PROGRESS, 0)
                        val name = it.progress.getString(FileUploadWorker.EXTRA_FILE_NAME) ?: ""
                        val error = it.progress.getString(FileUploadWorker.EXTRA_ERROR)

                        callback(name, progress, error)

                        if (it.state.isFinished) {
                            logD { "Upload finished: $name, cancelling worker." }
                            WorkManager.getInstance(context).cancelWorkById(id)
                        }
                    } ?: run {
                        callback("", -1, "Some error occurred")
                    }
                }
        }
    }

    suspend fun createFolder(parent: String, name: String): String? =
        driveService.createFolder(parent, name)

    @Synchronized
    fun downloadFile(cacheDir: File, fileId: String): Pair<Uri, String>? {
        val outputFile = File(cacheDir, fileId)
        try {
            val outputStream = FileOutputStream(outputFile)
            val mimeType = driveService.readTo(fileId, outputStream)
            outputStream.close()

            // Rename the file with the proper extension.
            val renamedFile = if (!mimeType.isNullOrEmpty()) {
                val newFile = File(cacheDir, "$fileId.${getExtension(mimeType)}")
                outputFile.renameTo(newFile)
                newFile
            } else {
                outputFile
            }
            return Pair(Uri.fromFile(renamedFile), mimeType!!)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun getExtension(mimeType: String): String? =
    MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

    suspend fun queryAllItems(context: Context, parent: String): List<DriveEntity>? =
        driveService.queryAll(context, parent)

    suspend fun deleteItem(context: Context, id: String): Boolean =
        driveService.delete(context, id)
}