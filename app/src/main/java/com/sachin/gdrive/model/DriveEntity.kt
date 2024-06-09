package com.sachin.gdrive.model

/**
 * Google drive entity.
 */
sealed class DriveEntity(open val id: String) {
    /**
     * For files.
     */
    data class File(override val id: String, val name: String) : DriveEntity(id)

    /**
     * For the folders.
     */
    data class Folder(override val id: String, val name: String) : DriveEntity(id)
}