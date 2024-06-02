package com.sachin.gdrive.adapter

import android.content.Context
import com.sachin.gdrive.model.DriveEntity

interface ItemClickListener {
    fun onFileClick(file: DriveEntity.File)
    fun onFolderClick(folder: DriveEntity.Folder)
    fun onItemLongClick(item: DriveEntity)
}