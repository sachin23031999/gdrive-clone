package com.sachin.gdrive.dashboard

import com.sachin.gdrive.model.DriveEntity

/**
 * Holds the state of the download.
 */
sealed class DownloadState {
    data class Downloading(val fileId: String, val progress: Int) : DownloadState()
    data class Downloaded(val fileId: String) : DownloadState()
    data class DownloadFailed(val fileId: String) : DownloadState()
}