package com.sachin.gdrive.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sachin.gdrive.auth.AuthScreen
import com.sachin.gdrive.dashboard.DashboardScreen

@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,
    start: String = Destination.AUTH_SCREEN.name,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = start
    ) {
        composable(Destination.AUTH_SCREEN.name) {
            AuthScreen(
                modifier = modifier,
                navController = navController
            )
        }
        composable(Destination.DASHBOARD_SCREEN.name) {
            DashboardScreen(
                modifier = modifier,
                navController = navController
            )
        }
    }
}