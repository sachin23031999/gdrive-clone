package com.sachin.gdrive.adapter

import com.sachin.gdrive.model.DriveEntity

interface ItemClickListener {
    fun onFileClick(fileName: String)
    fun onFolderClick(folderName: String, childFiles: List<DriveEntity>)
}