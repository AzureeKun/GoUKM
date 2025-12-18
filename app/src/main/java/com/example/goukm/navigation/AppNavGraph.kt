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
import com.example.goukm.ui.booking.BookingRequestScreen
import com.example.goukm.ui.userprofile.CustomerProfileScreen
import com.example.goukm.ui.userprofile.EditProfileScreen
import com.example.goukm.ui.userprofile.UserProfile
import com.example.goukm.ui.form.DriverApplicationFormScreen
import kotlinx.coroutines.launch
import com.example.goukm.ui.form.verificationIC
import com.example.goukm.ui.form.verificationDocuments
import com.example.goukm.ui.form.DriverApplicationStatusScreen
import com.example.goukm.ui.form.DriverApplicationViewModel
import com.example.goukm.ui.userprofile.DriverProfileScreen
import com.example.goukm.ui.driver.FareOfferScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.goukm.ui.chat.CustomerChatListScreen
import com.example.goukm.ui.chat.CustomerChatScreen
import com.example.goukm.ui.chat.DriverChatListScreen
import com.example.goukm.ui.chat.DriverChatScreen
import com.example.goukm.ui.driver.DriverEarningScreen
import com.example.goukm.ui.driver.DriverScoreScreen


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
    val driverApplicationStatus by authViewModel.driverApplicationStatus.collectAsState()
    val applicationViewModel: DriverApplicationViewModel = viewModel()

    // 🚀 Auto redirect based on authState + activeRole
    LaunchedEffect(authState, activeRole, driverApplicationStatus) {
        if (authState == AuthState.LoggedIn) {
            when {
                activeRole == "driver" -> navController.navigate(NavRoutes.DriverDashboard.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
                driverApplicationStatus == "rejected" -> {
                    // Only redirect rejected applications to status screen
                    navController.navigate(NavRoutes.DriverApplicationStatus.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                activeRole == "customer" || driverApplicationStatus == "under_review" -> {
                    // If application is under review, user stays as customer and goes to customer dashboard
                    navController.navigate(NavRoutes.CustomerDashboard.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
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
                onLoginSuccess = {
                    scope.launch {
                        authViewModel.fetchUserProfile(defaultToCustomer = true)
                        // ✅ DO NOT navigate here
                        // ✅ LaunchedEffect will handle navigation automatically
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
            RegisterOption(navController = navController, authViewModel = authViewModel)
        }

        // CUSTOMER DASHBOARD
        composable(NavRoutes.CustomerDashboard.route) {
            CustomerDashboard(
                navController = navController,
                userImageUrl = currentUser?.profilePictureUrl
            )
        }

        // BOOKING REQUEST
        composable(NavRoutes.BookingRequest.route) {
            BookingRequestScreen(navController = navController)
        }

        // DRIVER DASHBOARD
        composable(NavRoutes.DriverDashboard.route) {
            var localSelectedDriverNavIndex by remember { mutableStateOf(0) }
            DriverDashboard(
                navController = navController,
                authViewModel = authViewModel,
                selectedNavIndex = localSelectedDriverNavIndex,
                onNavSelected = { index ->
                    localSelectedDriverNavIndex = index
                    when (index) {
                        0 -> navController.navigate(NavRoutes.DriverDashboard.route) {
                            popUpTo(NavRoutes.DriverDashboard.route) { inclusive = true }
                        }
                        1 -> navController.navigate(NavRoutes.DriverScore.route)
                        2 -> navController.navigate(NavRoutes.DriverEarning.route)
                        3 -> navController.navigate(NavRoutes.DriverProfile.route)
                    }
                }
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
                    navController.navigate(NavRoutes.verificationIC.route) {
                        popUpTo(NavRoutes.CustomerProfile.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                authViewModel = authViewModel,
                applicationViewModel = applicationViewModel
            )
        }

        // LOADING
        composable(NavRoutes.Loading.route) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
            )
        }

        composable(NavRoutes.verificationIC.route) {
            verificationIC(
                onUploadComplete = {
                    navController.navigate(NavRoutes.verificationDocuments.route) {
                        popUpTo(NavRoutes.verificationIC.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(NavRoutes.verificationDocuments.route) {
            verificationDocuments(
                onUploadComplete = {
                    navController.navigate(NavRoutes.DriverApplicationStatus.route) {
                        popUpTo(NavRoutes.verificationDocuments.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
           )
        }

        composable(NavRoutes.DriverApplicationStatus.route) {
            DriverApplicationStatusScreen(
                status = driverApplicationStatus,
                onResubmit = {
                    navController.navigate(NavRoutes.DriverApplication.route) {
                        popUpTo(NavRoutes.DriverApplicationStatus.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBackToDashboard = {
                    navController.navigate(NavRoutes.CustomerDashboard.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        // DRIVER PROFILE
        composable(NavRoutes.DriverProfile.route) {
            DriverProfileScreen(
                navController = navController,
                user = currentUser,
                authViewModel = authViewModel,
                onEditProfile = { navController.navigate(NavRoutes.EditProfile.route) },
                onLogout = { authViewModel.logout() },
                selectedNavIndex = 3
            )
        }
        
        // FARE OFFER
        composable(
            route = "fare_offer/{customerName}/{pickup}/{dropOff}/{seats}/{bookingId}",
            arguments = listOf(
                navArgument("customerName") { type = NavType.StringType },
                navArgument("pickup") { type = NavType.StringType },
                navArgument("dropOff") { type = NavType.StringType },
                navArgument("seats") { type = NavType.IntType },
                navArgument("bookingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            FareOfferScreen(
                navController = navController,
                customerName = backStackEntry.arguments?.getString("customerName") ?: "",
                pickup = backStackEntry.arguments?.getString("pickup") ?: "",
                dropOff = backStackEntry.arguments?.getString("dropOff") ?: "",
                seats = backStackEntry.arguments?.getInt("seats") ?: 0,
                bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            )
        }
        
        // CONSUMER FARE OFFERS
        composable(
            route = "fare_offers_screen/{bookingId}",
            arguments = listOf(
                navArgument("bookingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            com.example.goukm.ui.booking.FareOffersScreen(
                navController = navController,
                bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            )
        }

        // CUSTOMER CHAT LIST
        composable(NavRoutes.CustomerChatList.route) {
            CustomerChatListScreen(navController = navController)
        }
        
        // CUSTOMER CHAT
        composable(
            route = "customer_chat/{chatId}/{contactName}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("contactName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            CustomerChatScreen(
                navController = navController,
                chatId = backStackEntry.arguments?.getString("chatId") ?: "",
                contactName = backStackEntry.arguments?.getString("contactName") ?: ""
            )
        }
        
        // DRIVER CHAT LIST
        composable(NavRoutes.DriverChatList.route) {
            DriverChatListScreen(navController = navController)
        }
        
        // DRIVER CHAT
        composable(
            route = "driver_chat/{chatId}/{contactName}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("contactName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            DriverChatScreen(
                navController = navController,
                chatId = backStackEntry.arguments?.getString("chatId") ?: "",
                contactName = backStackEntry.arguments?.getString("contactName") ?: ""
            )
        }
        
        // DRIVER EARNING
        composable(NavRoutes.DriverEarning.route) {
            DriverEarningScreen(navController = navController)
        }
        
        // DRIVER SCORE
        composable(NavRoutes.DriverScore.route) {
            DriverScoreScreen(navController = navController)
        }
        
        // DRIVER NAVIGATION
        composable(
            route = "driver_navigation_screen/{lat}/{lng}/{address}",
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType },
                navArgument("lng") { type = NavType.StringType },
                navArgument("address") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0
            val address = backStackEntry.arguments?.getString("address") ?: ""
            
            com.example.goukm.ui.driver.DriverNavigationScreen(
                navController = navController,
                pickupLat = lat,
                pickupLng = lng,
                pickupAddress = address
            )
        }
    }
}
