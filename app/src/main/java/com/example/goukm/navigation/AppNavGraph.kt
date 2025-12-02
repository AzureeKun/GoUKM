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
import com.example.goukm.ui.userprofile.EditProfileScreen
import com.example.goukm.ui.userprofile.UserProfile
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@Composable
fun AppNavGraph(navController: NavHostController) {

    var currentUser by remember {
        mutableStateOf(UserProfile("Siti Farhana", "A203399", email = "a203399@siswa.ukm.edu.my", phoneNumber = "019-8501780"))
    }

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
                user = currentUser,
                navController = navController,
                onEditProfile = { navController.navigate("edit_profile") }
            )
        }

        composable(NavRoutes.EditProfile.route) {
            EditProfileScreen(
                navController = navController,
                user = currentUser,
                onSave = { updatedUser ->
                    currentUser = updatedUser
                }
            )
        }
    }
}
