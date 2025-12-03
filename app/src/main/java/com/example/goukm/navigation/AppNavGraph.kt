package com.example.goukm.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.goukm.ui.register.*
import com.example.goukm.ui.dashboard.CustomerDashboard
import com.example.goukm.ui.dashboard.DriverDashboard
import com.example.goukm.ui.userprofile.CustomerProfileScreen
import com.example.goukm.ui.userprofile.EditProfileScreen
import com.example.goukm.ui.userprofile.UserProfile
import com.example.goukm.ui.form.DriverApplicationFormScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(LocalContext.current)
    )
) {
    val scope = rememberCoroutineScope()
    val authState by authViewModel.authState.collectAsState()
    val activeRole by authViewModel.activeRole.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // 🚀 Auto redirect based on authState + activeRole
    LaunchedEffect(authState, activeRole) {
        if (authState == AuthState.LoggedIn) {
            when (activeRole) {
                "driver" -> navController.navigate(NavRoutes.DriverDashboard.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
                "customer" -> navController.navigate(NavRoutes.CustomerDashboard.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
                else -> navController.navigate(NavRoutes.Register.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        } else if (authState == AuthState.LoggedOut) {
            navController.navigate(NavRoutes.Register.route) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Loading state UI
    if (authState == AuthState.Loading) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
        )
        return
    }

    // NavHost
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Loading.route
    ) {
        // REGISTER
        composable(NavRoutes.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                modifier = Modifier,
                navController = navController,
                onNavigateToName = {
                    navController.navigate(NavRoutes.NamePage.route)
                },
                onLoginSuccess = { role ->
                    scope.launch {
                        authViewModel.fetchUserProfile()
                        // Navigate to dashboard after login
                        if (role == "customer") {
                            navController.navigate(NavRoutes.CustomerDashboard.route) {
                                popUpTo(NavRoutes.Register.route) { inclusive = true }
                            }
                        } else if (role == "driver") {
                            navController.navigate(NavRoutes.DriverDashboard.route) {
                                popUpTo(NavRoutes.Register.route) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        // NAME PAGE
        composable(NavRoutes.NamePage.route) {
            NamePage(navController = navController)
        }

        // REGISTER OPTION
        composable(NavRoutes.RegisterOption.route) {
            RegisterOption(navController = navController)
        }

        // CUSTOMER DASHBOARD
        composable(NavRoutes.CustomerDashboard.route) {
            CustomerDashboard(navController)
        }

        // DRIVER DASHBOARD
        composable(NavRoutes.DriverDashboard.route) {
            var localSelectedDriverNavIndex by remember { mutableStateOf(0) }
            DriverDashboard(
                navController = navController,
                authViewModel = authViewModel,
                onSkip = { request -> println("Driver skipped ride: ${request.customerName}") },
                onOffer = { request -> println("Driver offered ride: ${request.customerName}") },
                selectedNavIndex = localSelectedDriverNavIndex,
                onNavSelected = { index -> localSelectedDriverNavIndex = index }
            )
        }

        // CUSTOMER PROFILE
        composable(NavRoutes.CustomerProfile.route) {
            CustomerProfileScreen(
                navController = navController,
                user = currentUser,
                authViewModel = authViewModel, // ✅ must pass this
                onEditProfile = { navController.navigate(NavRoutes.EditProfile.route) },
                onLogout = { authViewModel.logout() }
            )
        }

        // EDIT PROFILE
        composable(NavRoutes.EditProfile.route) {
            currentUser?.let { user ->
                EditProfileScreen(
                    navController = navController,
                    user = user,
                    onSave = { updatedUser ->
                        authViewModel.updateUserProfile(updatedUser)
                        navController.navigate(NavRoutes.CustomerProfile.route) {
                            popUpTo(NavRoutes.CustomerProfile.route) { inclusive = false }
                        }
                    }
                )
            } ?: CircularProgressIndicator(
                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
            )
        }

        // DRIVER APPLICATION FORM
        composable(NavRoutes.DriverApplication.route) {
            DriverApplicationFormScreen(
                navController = navController,
                onApplicationSubmit = {
                    navController.navigate(NavRoutes.CustomerProfile.route) {
                        popUpTo(NavRoutes.CustomerProfile.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // LOADING
        composable(NavRoutes.Loading.route) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
            )
        }
    }
}
