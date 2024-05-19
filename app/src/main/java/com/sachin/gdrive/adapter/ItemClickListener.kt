package com.sachin.gdrive.adapter

import com.sachin.gdrive.model.DriveEntity

interface ItemClickListener {
    fun onFileClick(file: DriveEntity.File)
    fun onFolderClick(folder: DriveEntity.Folder)
}