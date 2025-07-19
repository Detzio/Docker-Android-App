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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dockerapp.ui.screen.ContainerDetailsScreen
import com.example.dockerapp.ui.screen.CreateContainerScreen
import com.example.dockerapp.ui.screen.GrafanaScreen
import com.example.dockerapp.ui.screen.HomeScreen
import com.example.dockerapp.ui.screen.LogsScreen
import com.example.dockerapp.ui.screen.LoginScreen
import com.example.dockerapp.ui.screen.TerminalScreen
import com.example.dockerapp.ui.viewmodel.HomeViewModel
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
            val homeViewModel = viewModel<HomeViewModel>()
            val navigationEvent by homeViewModel.navigationEvent.collectAsState()
            val detailsNavigationEvent by homeViewModel.detailsNavigationEvent.collectAsState()

            // Observer pour détecter la navigation vers les logs
            LaunchedEffect(navigationEvent) {
                navigationEvent?.let { (containerId, containerName) ->
                    navController.navigate(AppScreen.Logs.createRoute(containerId, containerName))
                    homeViewModel.onNavigationHandled()
                }
            }
            
            // Observer pour détecter la navigation vers les détails
            LaunchedEffect(detailsNavigationEvent) {
                detailsNavigationEvent?.let { (containerId, containerName) ->
                    navController.navigate(AppScreen.ContainerDetails.createRoute(containerId, containerName))
                    homeViewModel.onDetailsNavigationHandled()
                }
            }
            
            HomeScreen(
                onLogout = handleLogout,
                onNavigateToCreateContainer = {
                    navController.navigate(AppScreen.CreateContainer.route)
                },
                onNavigateToGrafana = {
                    navController.navigate(AppScreen.Metrics.route)
                },
                homeViewModel = homeViewModel
            )
        }
          composable(
            AppScreen.Logs.route,
            arguments = listOf(
                navArgument("containerId") { type = NavType.StringType },
                navArgument("containerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val containerId = backStackEntry.arguments?.getString("containerId") ?: ""
            val containerName = backStackEntry.arguments?.getString("containerName") ?: ""
            LogsScreen(
                containerId = containerId,
                containerName = containerName,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(
            AppScreen.ContainerDetails.route,
            arguments = listOf(
                navArgument("containerId") { type = NavType.StringType },
                navArgument("containerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val containerId = backStackEntry.arguments?.getString("containerId") ?: ""
            val containerName = backStackEntry.arguments?.getString("containerName") ?: ""
            ContainerDetailsScreen(
                containerId = containerId,
                containerName = containerName,
                onBack = { navController.popBackStack() },
                onNavigateToTerminal = {id, name ->
                    navController.navigate("terminal/$id/$name")
                }
            )
        }

        composable (
          AppScreen.Terminal.route,
            arguments = listOf(
                navArgument("containerId") { type = NavType.StringType },
                navArgument("containerName") { type = NavType.StringType }
            )
        ) { navBackStackEntry ->
            val containerId = navBackStackEntry.arguments?.getString("containerId") ?: ""
            val containerName = navBackStackEntry.arguments?.getString("containerName") ?: ""
            TerminalScreen(
                containerId = containerId,
                containerName = containerName,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(AppScreen.CreateContainer.route) {
            CreateContainerScreen(
                onBack = { navController.popBackStack() },
                onContainerCreated = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(AppScreen.Metrics.route) {
            GrafanaScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class AppScreen(val route: String) {
    object Login : AppScreen("login")
    object Home : AppScreen("home")
    object Logs : AppScreen("logs/{containerId}/{containerName}") {
        fun createRoute(containerId: String, containerName: String): String = "logs/$containerId/${containerName}"
    }
    object ContainerDetails : AppScreen("container-details/{containerId}/{containerName}") {
        fun createRoute(containerId: String, containerName: String): String = "container-details/$containerId/${containerName}"
    }
    object Terminal: AppScreen("terminal/{containerId}/{containerName}")
    object CreateContainer : AppScreen("create-container")
    object Metrics : AppScreen("metrics")
}
