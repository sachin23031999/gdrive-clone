package com.sachin.gdrive.repository


import android.content.Context
import com.google.api.services.drive.model.File
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.AbstractInputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import com.sachin.gdrive.common.log.logD
import com.sachin.gdrive.common.log.logE
import com.sachin.gdrive.model.DriveEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Collections

/**
 * Performs read/write operations on Drive files.
 */
class DriveServiceProvider(
    private val authRepository: AuthRepository
) {

    private lateinit var driveService: Drive

    fun createService(context: Context) {
        val account = authRepository.getCurrentAccount(context)
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(
                DriveScopes.DRIVE_FILE
            )
        )
        credential.selectedAccount = account?.account
        driveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName("Drive API Migration")
            .build()
    }

    suspend fun read(fileId: String): ByteArray? {
        return try {
            // Perform the file download operation in the IO dispatcher
            withContext(Dispatchers.IO) {
                // Download the file content from Google Drive
                val outputStream = ByteArrayOutputStream()
                driveService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream)

                // Return the downloaded file content as a ByteArray
                outputStream.toByteArray()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    suspend fun write(
        context: Context,
        fileName: String,
        inputStream: InputStream,
        parentFolderId: String? = null,
        callback: ((progress: Int, error: String?) -> Unit)
    ) {
        try {
            val file = File().apply {
                name = fileName
                parentFolderId?.let { parents = listOf(it) }
            }
            val fileContent = object : AbstractInputStreamContent("application/octet-stream") {
                override fun getLength(): Long = inputStream.available().toLong()
                override fun retrySupported(): Boolean = true
                override fun getInputStream(): InputStream = inputStream
                override fun writeTo(outputStream: OutputStream) {
                    inputStream.use { it.copyTo(outputStream) }
                }
            }

            driveService.files().create(file, fileContent).apply {
                mediaHttpUploader.setProgressListener { uploader ->
                    callback((uploader.progress * 100).toInt(), null)
                }
                setFields("id").execute().id
            }
        } catch (ue: UserRecoverableAuthIOException) {
            callback(0, "Unauthorized, starting user consent dialog")
            context.startActivity(ue.intent)
        } catch (e: GoogleJsonResponseException) {
            if (e.statusCode == 404) {
                callback(0, "Parent folder not found")
            } else {
                callback(0, "Failed to upload file")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun queryChildNodes(
        context: Context,
        driveService: Drive,
        parentFolderId: String
    ): List<DriveEntity>? {
        return try {
            withContext(Dispatchers.IO) {
                val rootEntities = mutableListOf<DriveEntity>()

                // Query all files from Google Drive
                val result: FileList = driveService.files().list()
                    .setPageSize(10)
                    .setQ("'$parentFolderId' in parents") // Filter by parent folder ID
                    .setFields("nextPageToken, files(id, name, mimeType, parents)")
                    .execute()

                // Build the hierarchy
                buildHierarchy(rootEntities, result.files, driveService)
                rootEntities
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun buildHierarchy(
        parentEntities: MutableList<DriveEntity>,
        files: List<File>,
        driveService: Drive
    ) {
        for (file in files) {
            // Only process next-level children
            if (file.parents?.get(0) == null) {
                parentEntities.add(createEntity(file, driveService))
            } else {
                val parentId = file.parents[0]
                val parentFolder =
                    parentEntities.find { it is DriveEntity.Folder && it.id == parentId }

                if (parentFolder != null && parentFolder is DriveEntity.Folder) {
                    parentFolder.children.add(createEntity(file, driveService))
                }
            }
        }
    }

    private suspend fun createEntity(file: File, driveService: Drive): DriveEntity {
        return if (file.mimeType == "application/vnd.google-apps.folder") {
            val children = mutableListOf<DriveEntity>()
            val fileList = driveService.files().list().setQ("'${file.id}' in parents").execute()
            buildHierarchy(children, fileList.files, driveService)
            DriveEntity.Folder(file.id, file.name, children)
        } else {
            DriveEntity.File(file.id, file.name)
        }
    }
}