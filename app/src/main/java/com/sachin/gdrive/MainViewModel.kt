package com.sachin.gdrive

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sachin.gdrive.auth.AuthState
import com.sachin.gdrive.repository.AuthRepository

/**
 * Main view model class.
 */
class MainViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _authState = MutableLiveData<AuthState>()

    val authState: LiveData<AuthState> = _authState
    fun init(context: Context) {
        authRepository.initialise(context)
    }

    /**
     * Checks if user is already signed in.
     */
    fun checkLogin(context: Context) {
        authRepository.apply {
            if (isUserSignedIn(context)) {
                _authState.postValue(AuthState.AlreadyLoggedIn)
            } else {
                _authState.postValue(AuthState.NotLoggedIn)
            }
        }
    }
}