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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState


@Composable
fun AppNavGraph(navController: NavHostController) {

    var currentUser by remember {
        mutableStateOf(UserProfile("Siti Farhana", "A203399", email = "a203399@siswa.ukm.edu.my", phoneNumber = "019-8501780"))
    }

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(LocalContext.current)
    )

    val authState by authViewModel.authState.collectAsState()

    val startDestination = when (authState) {
        is AuthState.Loading -> null
        is AuthState.LoggedIn -> NavRoutes.CustomerDashboard.route
        is AuthState.LoggedOut -> NavRoutes.Login.route // Assuming you add NavRoutes.Login
    }

    if (startDestination == null) {
        CircularProgressIndicator()
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
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
                onEditProfile = { navController.navigate(NavRoutes.EditProfile.route) },
                // 👈 Add Logout handler
                onLogout = {
                    authViewModel.logout() // Clear token and update state to LoggedOut
                    navController.navigate(NavRoutes.Login.route) {
                        // Clear all previous screens, leaving only Login
                        popUpTo(NavRoutes.CustomerDashboard.route) { inclusive = true }
                    }
                }
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
