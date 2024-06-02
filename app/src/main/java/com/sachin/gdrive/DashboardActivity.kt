package com.sachin.gdrive

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.sachin.gdrive.auth.AuthState
import com.sachin.gdrive.common.log.logD
import com.sachin.gdrive.ui.MainNavigation
import com.sachin.gdrive.ui.Destination
import com.sachin.gdrive.ui.theme.AppTheme
import org.koin.android.ext.android.inject

/**
 * Main activity for the application.
 */
class DashboardActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by inject()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logD { "on create" }
        enableEdgeToEdge()
        setStartScreen(Destination.DASHBOARD_SCREEN)
        setupObserver()
        viewModel.init(this)
        viewModel.checkLogin(this)
    }

    private fun setStartScreen(startDest: Destination) {
        setContent {
            Scaffold {
                AppTheme {
                    MainNavigation(
                        modifier = Modifier.padding(it),
                        start = startDest.name
                    )
                }
            }

        }
    }

    private fun setupObserver() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.AlreadyLoggedIn -> {
                    logD { "Already logged in" }
                    setStartScreen(Destination.DASHBOARD_SCREEN)
                }

                is AuthState.NotLoggedIn -> {
                    logD { "Not logged in" }
                    setStartScreen(Destination.AUTH_SCREEN)
                }

                else -> {}
            }
        }
    }
}