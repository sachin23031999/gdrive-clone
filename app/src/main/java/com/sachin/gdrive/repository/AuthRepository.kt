package com.sachin.gdrive.repository

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.sachin.gdrive.auth.AccountInfo
import com.sachin.gdrive.common.log.logD
import com.sachin.gdrive.common.log.logE
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Authentication related repository.
 */
class AuthRepository {

    private var googleSignInClient: GoogleSignInClient? = null
    private var isInitialised = false
    fun initialise(context: Context) {
        if (isInitialised) {
            logD { "Already initialised" }
            return
        }
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient =
            GoogleSignIn.getClient(context, gso)
        isInitialised = true
    }

    fun getSignInIntent() = googleSignInClient?.signInIntent

    fun getSignedInAccount(intent: Intent): AccountInfo? =
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            task.getResult(ApiException::class.java)?.let { account ->
                map(account)
            }
        } catch (e: Exception) {
            logE { "Exception occurred:" }
            e.printStackTrace()
            null
        }

    fun isUserSignedIn(context: Context): Boolean =
        GoogleSignIn.getLastSignedInAccount(context)?.isExpired == false

    suspend fun logout(): Boolean {
        return googleSignInClient?.let { client ->
            suspendCancellableCoroutine<Boolean> { continuation ->
                val signOutTask = client.signOut()
                signOutTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(true)
                    } else {
                        continuation.resume(false)
                    }
                }
            }
        } ?: false
    }

    private fun map(account: GoogleSignInAccount) = AccountInfo(
        authToken = account.idToken ?: "",
        fullName = account.displayName,
        email = account.email,
    )

    fun getCurrentAccount(context: Context) =
        GoogleSignIn.getLastSignedInAccount(context)
}