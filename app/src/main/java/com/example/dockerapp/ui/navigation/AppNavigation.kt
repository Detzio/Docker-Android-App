package com.example.dockerapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dockerapp.ui.screen.HomeScreen
import com.example.dockerapp.ui.screen.LoginScreen
import com.example.dockerapp.ui.viewmodel.LoginViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    loginViewModel: LoginViewModel = viewModel()
) {
    val isAuthenticated by loginViewModel.isAuthenticated.collectAsState()
    var isLoggingOut by remember { mutableStateOf(false) }
    
    // Observer pour détecter la déconnexion
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated && isLoggingOut) {
            // Si on vient de se déconnecter, naviguer vers la page de connexion
            navController.navigate(AppScreen.Login.route) {
                popUpTo(0) { inclusive = true } // Effacer tout le stack de navigation
            }
            isLoggingOut = false
        }
    }
    
    val handleLogout = remember {
        {
            isLoggingOut = true
            loginViewModel.logout()
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) AppScreen.Home.route else AppScreen.Login.route
    ) {
        composable(AppScreen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                navigateToHome = {
                    navController.navigate(AppScreen.Home.route) {
                        popUpTo(AppScreen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(AppScreen.Home.route) {
            HomeScreen(
                onLogout = handleLogout,
                viewModel = loginViewModel
            )
        }
    }
}

sealed class AppScreen(val route: String) {
    object Login : AppScreen("login")
    object Home : AppScreen("home")
}
