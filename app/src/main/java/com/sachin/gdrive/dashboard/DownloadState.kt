package com.sachin.gdrive.dashboard

import com.sachin.gdrive.model.DriveEntity

sealed class DownloadState {
    data class DOWNLOADING(val fileId: String, val progress: Int) : DownloadState()
    data class DOWNLOADED(val fileId: String) : DownloadState()
    data class DOWNLOAD_FAILED(val fileId: String) : DownloadState()

    data class DOWNLOADED_ALL(val list: List<DriveEntity>) : DownloadState()
}