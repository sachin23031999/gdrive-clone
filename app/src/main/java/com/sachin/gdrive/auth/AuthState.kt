package com.sachin.gdrive.auth

/**
 * Holds the state for authentication flow.
 */
sealed class AuthState {

    data class SignInSuccess(val info: AccountInfo) : AuthState()

    data class SignInFailed(val error: String) : AuthState()

    data object AlreadyLoggedIn : AuthState()

    data object NotLoggedIn : AuthState()
}