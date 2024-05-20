package com.sachin.gdrive.auth

/**
 * Holds the user account info.
 */
data class AccountInfo(
    val authToken: String,
    val fullName: String?,
    val email: String?
)