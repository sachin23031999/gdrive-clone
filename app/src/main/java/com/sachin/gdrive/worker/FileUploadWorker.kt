package com.sachin.gdrive.worker

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.sachin.gdrive.common.Utils
import com.sachin.gdrive.common.Worker
import com.sachin.gdrive.common.Worker.FILE_UPLOAD_WORKER_NAME
import com.sachin.gdrive.common.Worker.WORKER_HOUR
import com.sachin.gdrive.common.Worker.WORKER_MINUTE
import com.sachin.gdrive.common.Worker.WORKER_REPEAT_INTERVAL_IN_HOURS
import com.sachin.gdrive.common.Worker.WORKER_SECOND
import com.sachin.gdrive.common.log.logD
import com.sachin.gdrive.common.log.logE
import com.sachin.gdrive.common.log.logI
import com.sachin.gdrive.notification.NotificationManager
import com.sachin.gdrive.provider.DriveServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException
import java.io.InputStream
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class FileUploadWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters), KoinComponent {

    private val driveService: DriveServiceProvider by inject()
    private val notificationManager: NotificationManager by inject()

    override suspend fun doWork(): Result {
        logD { "doWork()" }
        val fileName = inputData.getString(EXTRA_FILE_NAME)
        val fileUri = inputData.getString(EXTRA_FILE_URI)
        val parentFolderId = inputData.getString(EXTRA_PARENT_FOLDER_ID)
        if (fileName == null || fileUri == null || parentFolderId == null) {
            return Result.failure()
        }

        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                val notifId = UUID.randomUUID().mostSignificantBits.toInt()
                var isResumed = false
                val resumeOnce: (Result) -> Unit = { result ->
                    if (!isResumed) {
                        isResumed = true
                        if (result == Result.success()) {
                            continuation.resume(Result.success())
                        } else {
                            setProgressAsync(
                                workDataOf(
                                    EXTRA_FILE_NAME to fileName,
                                    EXTRA_ERROR to "Upload failed"
                                )
                            ).get()
                            continuation.resume(Result.failure())
                        }
                    }
                }
                if (driveService.createService(context)) {
                    uploadFile(
                        context, fileName, Uri.parse(fileUri), parentFolderId
                    ) { progress, error ->
                        handleUploadProgress(
                            fileName = fileName,
                            notifId = notifId,
                            progress = progress,
                            error = error,
                            resumeOnce = resumeOnce
                        )
                    }
                } else {
                    resumeOnce(Result.failure())
                }

                continuation.invokeOnCancellation {
                    notificationManager.dismissUploadNotification(notifId)
                }
            }
        }
    }

    private fun handleUploadProgress(
        fileName: String,
        notifId: Int,
        progress: Int,
        error: String?,
        resumeOnce: (Result) -> Unit
    ) {
        try {
            if (error != null) {
                logD { "Error: $error" }
                resumeOnce(Result.failure())
            } else {
                logD { "$fileName progress: $progress" }
                setProgressAsync(
                    workDataOf(
                        EXTRA_FILE_NAME to fileName,
                        EXTRA_PROGRESS to progress
                    )
                ).get()
                notificationManager.showUploadNotification(
                    notifId = notifId,
                    title = "Uploading file",
                    desc = fileName,
                    progress = progress
                )
                if (progress == TOTAL_PROGRESS) {
                    notificationManager.dismissUploadNotification(notifId)
                    resumeOnce(Result.success())
                }
            }
        } catch (e: Exception) {
            logD { "exception in handle upload progress ${e.message}" }
            e.printStackTrace()
            notificationManager.dismissUploadNotification(notifId)
            setProgressAsync(
                workDataOf(
                    EXTRA_FILE_NAME to fileName,
                    EXTRA_ERROR to "Some error occurred"
                )
            ).get()

            resumeOnce(Result.failure())
        }
    }

    private fun uploadFile(
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

    companion object {
        const val EXTRA_FILE_NAME = "file_name"
        const val EXTRA_FILE_URI = "file_uri"
        const val EXTRA_PARENT_FOLDER_ID = "folder_id"
        const val EXTRA_PROGRESS = "progress"
        const val EXTRA_ERROR = "error"
        const val TOTAL_PROGRESS = 100

        fun enqueue(context: Context, data: Data): UUID {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val periodicWorkRequest =
                PeriodicWorkRequestBuilder<FileUploadWorker>(
                    WORKER_REPEAT_INTERVAL_IN_HOURS,
                    TimeUnit.HOURS
                ).apply {
                    setInputData(data)
                    setConstraints(constraints)
                }.build()

            logI { "repeat interval: $WORKER_REPEAT_INTERVAL_IN_HOURS hrs" }
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    FILE_UPLOAD_WORKER_NAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    periodicWorkRequest
                )
            return periodicWorkRequest.id
        }
    }
}
