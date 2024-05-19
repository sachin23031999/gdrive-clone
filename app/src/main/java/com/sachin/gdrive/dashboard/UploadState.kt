package com.sachin.gdrive.dashboard

sealed class UploadState {
    data class Uploading(val fileName: String, val progress: Int) : UploadState()
    data class Uploaded(val fileName: String) : UploadState()
    data class Failed(val fileName: String, val error: String) : UploadState()
}