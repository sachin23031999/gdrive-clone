package com.sachin.gdrive.model

sealed class DriveEntity {
    data class File(val id: String, val name: String) : DriveEntity()
    data class Folder(val id: String, val name: String, val children: MutableList<DriveEntity>) : DriveEntity()
}