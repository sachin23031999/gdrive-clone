package com.sachin.gdrive.dashboard

import android.net.Uri
import com.sachin.gdrive.model.DriveEntity

/**
 * Holds the state of the download.
 */
sealed class DownloadState {
    data class Started(val file: DriveEntity.File): DownloadState()
    data class Downloaded(val file: DriveEntity.File, val fileUri: Uri, val mimeType: String) : DownloadState()
    data class Failed(val file: DriveEntity.File) : DownloadState()
}