package com.sachin.gdrive.dashboard

/**
 * Holds the state of an upload.
 */
sealed class UploadState {
    data class Started(val fileName: String) : UploadState()

    data class Uploading(val fileName: String, val progress: Int) : UploadState()
    data class Uploaded(val fileName: String) : UploadState()
    data class Failed(val fileName: String, val error: String) : UploadState()
}