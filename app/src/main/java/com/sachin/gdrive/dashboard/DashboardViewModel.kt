package com.sachin.gdrive.dashboard

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sachin.gdrive.repository.DriveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(
    private val driveRepository: DriveRepository
) : ViewModel() {

    private val _uploadState = MutableLiveData<UploadState>()
    private val _downloadState = MutableLiveData<DownloadState>()

    val uploadState: LiveData<UploadState> = _uploadState

    fun init(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                driveRepository.initialise(context)
            }
        }
    }

    fun startUpload(context: Context, parentFolder: String?, fileName: String, fileUri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                driveRepository
                    .uploadFile(context, fileName, fileUri, parentFolder) { progress, error ->
                        error?.let {
                            _uploadState.postValue(UploadState.Failed(fileName, it))
                            return@uploadFile
                        }

                        if (progress.toInt() == 100) {
                            _uploadState.postValue(UploadState.Uploaded(fileName))
                        } else {
                            _uploadState.postValue(UploadState.Uploading(fileName, progress))
                        }
                    }
            }
        }
    }
}