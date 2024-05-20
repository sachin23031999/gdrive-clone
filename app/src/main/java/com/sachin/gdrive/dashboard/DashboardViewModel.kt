package com.sachin.gdrive.dashboard

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sachin.gdrive.common.log.logD
import com.sachin.gdrive.repository.DriveRepository
import com.sachin.gdrive.worker.FileUploadWorker.Companion.TOTAL_PROGRESS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(
    private val driveRepository: DriveRepository
) : ViewModel() {

    private val _uploadState = MutableLiveData<UploadState>()
    private val _downloadState = MutableLiveData<DownloadState>()
    private val _uiState = MutableLiveData<DashboardState>()
    private val _createFolderState = MutableLiveData<Boolean>()
    private val _deleteState = MutableLiveData<Boolean>()
    val uploadState: LiveData<UploadState> = _uploadState
    val uiState: LiveData<DashboardState> = _uiState
    val createFolderState: LiveData<Boolean> = _createFolderState
    val deleteState: LiveData<Boolean> = _deleteState

    fun init(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val success = driveRepository.initialise(context)
                _uiState.postValue(
                    if (success)
                        DashboardState.InitSuccess
                    else
                        DashboardState.InitFailed
                )
            }
        }
    }

    fun startUpload(context: Context, parentId: String, fileName: String, fileUri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                _uploadState.postValue(UploadState.Started(fileName))
                driveRepository
                    .uploadFile(context, fileName, fileUri, parentId) { name, progress, error ->
                        logD { "upload status: $name $progress $error" }
                        if (error != null) {
                            _uploadState.postValue(
                                UploadState.Failed(name, error)
                            )
                            return@uploadFile
                        }

                        if (progress < TOTAL_PROGRESS) {
                            _uploadState.postValue(
                                UploadState.Uploading(name, progress)
                            )
                        } else {
                            _uploadState.postValue(
                                UploadState.Uploaded(name)
                            )
                        }
                    }
            }
        }
    }

    /**
     * Fetches all files and folders.
     */
    fun fetchAll(context: Context, parent: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _uiState.postValue(
                    DashboardState.FetchSuccess(
                        driveRepository.queryAllItems(context, parent)
                    )
                )
            }
        }
    }

    /**
     * Creates empty folder in the current folder.
     */
    fun createFolder(currentFolder: String, name: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _createFolderState.postValue(
                    driveRepository.createFolder(currentFolder, name) != null
                )
            }
        }
    }

    fun deleteItem(context: Context, item: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _deleteState.postValue(
                    driveRepository.deleteItem(context, item)
                )
            }
        }
    }
}