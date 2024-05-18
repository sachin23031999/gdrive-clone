package com.sachin.gdrive.auth

data class AccountInfo(
    val authToken: String,
    val fullName: String?,
    val email: String?
)