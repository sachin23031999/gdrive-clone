package com.sachin.gdrive

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.sachin.gdrive.auth.AuthActivity
import com.sachin.gdrive.auth.AuthState
import com.sachin.gdrive.common.showToast
import com.sachin.gdrive.databinding.ActivityMainBinding
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: MainViewModel by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupObserver()
        viewModel.init(this)
        viewModel.checkLogin(this)
    }

    private fun setupObserver() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.AlreadyLoggedIn -> {
                    showToast("Already logged in!")
                    // TODO: navigate to dashboard
                }

                is AuthState.NotLoggedIn -> {
                    // navigate to auth activity
                    startActivity(Intent(this, AuthActivity::class.java))
                }

                else -> {}
            }
        }
    }

}