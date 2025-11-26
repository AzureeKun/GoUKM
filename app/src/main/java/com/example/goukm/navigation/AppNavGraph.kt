package com.example.goukm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.goukm.ui.register.*
import com.example.goukm.ui.dashboard.CustomerDashboard
import com.example.goukm.ui.dashboard.DriverDashboard
import com.example.goukm.ui.userprofile.CustomerProfileScreen
import com.example.goukm.ui.userprofile.UserProfile

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Register.route
    ) {

        composable(NavRoutes.Register.route) {
            RegisterScreen(
                modifier = Modifier,
                onNavigateToName = {
                    navController.navigate(NavRoutes.NamePage.route)
                },
                onLoginSuccess = { role ->
                    if (role == "customer")
                        navController.navigate(NavRoutes.CustomerDashboard.route)
                    else
                        navController.navigate(NavRoutes.DriverDashboard.route)
                }
            )
        }

        composable(NavRoutes.NamePage.route) {
            NamePage(
                onNavigateToRolePage = {
                    navController.navigate(NavRoutes.RegisterOption.route)
                }
            )
        }

        composable(NavRoutes.RegisterOption.route) {
            RegisterOption(
                onRegisterSuccess = { role ->
                    if (role == "customer")
                        navController.navigate(NavRoutes.CustomerDashboard.route)
                    else
                        navController.navigate(NavRoutes.DriverDashboard.route)
                }
            )
        }

        composable(NavRoutes.CustomerDashboard.route) {
            CustomerDashboard(navController)
        }

        composable(NavRoutes.DriverDashboard.route) {
            DriverDashboard(navController)
        }

        composable(NavRoutes.CustomerProfile.route) {
            CustomerProfileScreen(
                user = UserProfile("Ahmad Bin Abu", "A18CS0123"),
                navController = navController
            )
        }
    }
}
