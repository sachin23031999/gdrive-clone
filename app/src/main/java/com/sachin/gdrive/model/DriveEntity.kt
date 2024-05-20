package com.sachin.gdrive.model

/**
 * Google drive entity.
 */
sealed class DriveEntity {
    /**
     * For files.
     */
    data class File(val id: String, val name: String) : DriveEntity()

    /**
     * For the folders.
     */
    data class Folder(val id: String, val name: String) : DriveEntity()
}