package com.sachin.gdrive

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.sachin.gdrive.MainViewModel
import com.sachin.gdrive.R
import com.sachin.gdrive.auth.AuthState
import com.sachin.gdrive.common.log.logD
import com.sachin.gdrive.databinding.ActivityDashboardBinding
import org.koin.android.ext.android.inject

/**
 * Main activity for the application.
 */
class DashboardActivity : AppCompatActivity() {

    private val binding by lazy { ActivityDashboardBinding.inflate(layoutInflater) }
    private val viewModel: MainViewModel by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logD { "on create" }
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
                    logD { "Already logged in" }
                    setNavigationGraph(
                        startFragment = R.id.dashboardFragment
                    )
                }

                is AuthState.NotLoggedIn -> {
                    logD { "Not logged in" }
                    setNavigationGraph(
                        startFragment = R.id.signInFragment
                    )
                }

                else -> {}
            }
        }
    }

    private fun setNavigationGraph(startFragment: Int) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph_dashboard)

        navGraph.setStartDestination(startFragment)
        navController.setGraph(navGraph, intent.extras)
    }
}