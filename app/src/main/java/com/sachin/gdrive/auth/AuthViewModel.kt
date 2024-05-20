package com.sachin.gdrive.auth

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sachin.gdrive.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * View model for Authentication operations.
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()

    val authState: LiveData<AuthState> = _authState

    fun init(context: Context) {
        authRepository.initialise(context)
    }
    fun checkLogin(context: Context) {
        viewModelScope.launch {
            authRepository.apply {
                if (isUserSignedIn(context)) {
                    _authState.postValue(AuthState.AlreadyLoggedIn)
                } else {
                    _authState.postValue(AuthState.NotLoggedIn)
                }
            }
        }
    }

    fun getSignInIntent() = authRepository.getSignInIntent()

    fun onSignIn(intent: Intent?) {
        intent?.let {
            authRepository.getSignedInAccount(it)?.let { info ->
                _authState.postValue(AuthState.SignInSuccess(info))
            }
                ?: _authState.postValue(AuthState.SignInFailed("Error"))
        } ?: _authState.postValue(AuthState.SignInFailed("Error"))
    }
}