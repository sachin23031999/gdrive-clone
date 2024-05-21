package com.sachin.gdrive.provider


import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.AbstractInputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.sachin.gdrive.common.log.logD
import com.sachin.gdrive.model.DriveEntity
import com.sachin.gdrive.repository.AuthRepository
import java.io.FileOutputStream
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

    private lateinit var gdrive: Drive

    fun createService(context: Context): Boolean = try {
        val account = authRepository.getCurrentAccount(context)
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(
                DriveScopes.DRIVE_FILE
            )
        )
        credential.selectedAccount = account?.account

        gdrive = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName("Drive API Migration")
            .build()
        true
    } catch (_: Exception) {
        false
    }

    fun readTo(fileId: String, outputStream: FileOutputStream): String? {
        return try {
            val meta = gdrive.files().get(fileId)
                .setFields("mimeType, name")
                .execute()

            gdrive.files().get(fileId)
                .executeMediaAndDownloadTo(outputStream)

            meta.mimeType
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun write(
        context: Context,
        fileName: String,
        inputStream: InputStream,
        parentFolderId: String = ROOT_FOLDER_ID,
        callback: ((progress: Int, error: String?) -> Unit)
    ) {
        try {
            val file = File().apply {
                name = fileName
                parents = listOf(parentFolderId)
            }
            val fileContent = object : AbstractInputStreamContent("application/octet-stream") {
                override fun getLength(): Long = inputStream.available().toLong()
                override fun retrySupported(): Boolean = true
                override fun getInputStream(): InputStream = inputStream
                override fun writeTo(outputStream: OutputStream) {
                    inputStream.use { it.copyTo(outputStream) }
                }
            }

            gdrive.files().create(file, fileContent).apply {
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

    suspend fun createFolder(
        parentFolderId: String = ROOT_FOLDER_ID,
        folderName: String
    ): String? {
        return try {
            val folderMetadata = File().apply {
                name = folderName
                mimeType = "application/vnd.google-apps.folder"
                parents = listOf(parentFolderId)
            }

            val folder = gdrive.files().create(folderMetadata)
                .setFields("id")
                .execute()

            folder.id
        } catch (e: GoogleJsonResponseException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    suspend fun queryAll(
        context: Context,
        parentFolderId: String
    ): List<DriveEntity>? = try {
        logD { "queryAll()" }
        val driveEntities = mutableListOf<DriveEntity>()

        val result: FileList = gdrive.files().list()
            .setQ("trashed=false and '$parentFolderId' in parents")
            .setFields("files(id, name, mimeType), nextPageToken")
            .setPageSize(100) // Max number of results per page
            .execute()

        result.files?.forEach { item ->
            logD { "Item: ${item.name}, MIME type: ${item.mimeType}" }
            if (item.mimeType == "application/vnd.google-apps.folder") {
                driveEntities.add(DriveEntity.Folder(item.id, item.name))
            } else {
                driveEntities.add(DriveEntity.File(item.id, item.name))
            }
        }
        driveEntities
    } catch (ue: UserRecoverableAuthIOException) {
        context.startActivity(ue.intent)
        null
    } catch (e: GoogleJsonResponseException) {
        if (e.statusCode == 404) {
            null
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    suspend fun delete(context: Context, id: String): Boolean =
        try {
            gdrive.files().delete(id).execute()
            true
        } catch (ue: UserRecoverableAuthIOException) {
            context.startActivity(ue.intent)
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }


    companion object {
        const val ROOT_FOLDER_ID = "root"
    }
}