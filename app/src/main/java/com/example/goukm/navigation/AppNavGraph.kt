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
import com.example.goukm.ui.booking.confirmPay
import com.example.goukm.ui.booking.RideDoneScreen
import com.example.goukm.ui.booking.PaymentQRScreen
import com.example.goukm.ui.driver.JourneySummaryScreen
import com.example.goukm.ui.booking.PaymentMethodScreen
import com.example.goukm.ui.journey.CustomerJourneyDetailsScreen
import com.example.goukm.navigation.NavRoutes.CustomerJourneyDetailsScreen
import com.example.goukm.ui.history.CustomerBookingHistoryScreen
import com.example.goukm.ui.history.DriverRideBookingHistoryScreen

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
                activeRole == "customer" -> {
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

        // CUSTOMER BOOKING HISTORY
        composable(NavRoutes.CustomerBookingHistory.route) {
            CustomerBookingHistoryScreen()
        }

        // DRIVER RIDE HISTORY
        composable(NavRoutes.DriverRideHistory.route) {
            DriverRideBookingHistoryScreen()
        }

        // BOOKING REQUEST
        composable(
            route = NavRoutes.BookingRequest.route,
            arguments = listOf(
                navArgument("bookingId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            BookingRequestScreen(
                navController = navController,
                activeBookingId = backStackEntry.arguments?.getString("bookingId")
            )
        }

// DRIVER JOURNEY SUMMARY - Navigate from DriverDashboard
        composable(
            route = "driver_journey_summary/{bookingId}",
            arguments = listOf(
                navArgument("bookingId") {
                    type = NavType.StringType
                    defaultValue = "default_booking"
                }
            )
        )
        { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: "default_booking"
            JourneySummaryScreen(
                navController = navController,
                bookingId = bookingId
            )
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

        //VERIFY IC DETAILS
        composable(NavRoutes.verificationIC.route) {
            verificationIC(
                navController = navController,
                onUploadComplete = {
                    navController.navigate(NavRoutes.verificationDocuments.route)
                },
                viewModel = applicationViewModel
            )
        }

        //VERIFY DOCUMENTS DETAILS
        composable(NavRoutes.verificationDocuments.route) {
            verificationDocuments(
                navController = navController,
                onUploadComplete = {
                    navController.navigate(NavRoutes.DriverApplicationStatus.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                viewModel = applicationViewModel
            )
        }

        //DRIVER APPLICATION STATUS
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
                    navController.navigate(NavRoutes.CustomerProfile.route) {
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

        // CONFIRM PAYMENT SCREEN
        composable(
            route = "confirm_pay/{paymentMethod}/{bookingId}",
            arguments = listOf(
                navArgument("paymentMethod") { type = NavType.StringType },
                navArgument("bookingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val paymentMethod = backStackEntry.arguments?.getString("paymentMethod") ?: "CASH"
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            val bookingRepository = remember { com.example.goukm.ui.booking.BookingRepository() }
            
            confirmPay(
                totalAmount = "RM 25",
                paymentMethod = paymentMethod,
                onProceedPayment = { },
                oncashConfirmation = {
                     scope.launch {
                         bookingRepository.updatePaymentStatus(bookingId, "PAID")
                         navController.navigate("ride_done/$bookingId") {
                             popUpTo("confirm_pay/{paymentMethod}/{bookingId}") { inclusive = true }
                         }
                     }
                },
                onNavigateToQR = {
                    navController.navigate("payment_qr/RM 25/$bookingId")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }


        // PAYMENT METHOD SCREEN
        composable(
            route = "payment-method/{amount}",
            arguments = listOf(
                navArgument("amount") {
                    type = NavType.StringType
                    defaultValue = "RM 0"
                }
            )
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "RM 0"
            com.example.goukm.ui.booking.PaymentMethodScreen(
                totalAmount = amount,
                onPaymentConfirmed = { method ->
                    navController.navigate("payment-success/$method")
                },
            )
        }

        // RIDE DONE SCREEN
        composable(
            route = NavRoutes.RideDoneScreen.route,
            arguments = listOf(
                navArgument("bookingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            RideDoneScreen(
                navController = navController,
                bookingId = bookingId,
                onFeedbackSubmitted = { rating, comment ->
                    // Navigation handled inside the screen or here
                    navController.navigate(NavRoutes.CustomerDashboard.route) {
                        popUpTo(NavRoutes.CustomerDashboard.route) { inclusive = true }
                    }
                }
            )
        }



        //CUSTOMER JOURNEY
        composable(
            route = "cust_journey_details/{bookingId}/{paymentMethod}?paymentStatus={paymentStatus}",
            arguments = listOf(
                navArgument("bookingId") { type = NavType.StringType },
                navArgument("paymentMethod") { type = NavType.StringType },
                navArgument("paymentStatus") { type = NavType.StringType; defaultValue = "PENDING" }
            )
        ) { backStackEntry ->
            val paymentMethod = backStackEntry.arguments?.getString("paymentMethod") ?: "CASH"
            val paymentStatus = backStackEntry.arguments?.getString("paymentStatus") ?: "PENDING"
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            
            CustomerJourneyDetailsScreen(
                onChatClick = { chatId, name, phone ->
                    val encodedName = java.net.URLEncoder.encode(name, "UTF-8")
                    val encodedPhone = java.net.URLEncoder.encode(phone, "UTF-8")
                    navController.navigate("customer_chat/$chatId/$encodedName/$encodedPhone")
                },
                navController = navController,
                paymentMethod = paymentMethod,
                initialPaymentStatus = paymentStatus
            )
        }
        

        // PAYMENT QR SCREEN
        composable(
            route = "payment_qr/{amount}/{bookingId}",
            arguments = listOf(
                navArgument("amount") { type = NavType.StringType },
                navArgument("bookingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0.00"
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            val bookingRepository = remember { com.example.goukm.ui.booking.BookingRepository() }
            val scope = rememberCoroutineScope()
            
            PaymentQRScreen(
                totalAmount = amount,
                bookingId = bookingId,
                onPaymentCompleted = {
                    scope.launch {
                        bookingRepository.updatePaymentStatus(bookingId, "PAID")
                        navController.navigate("ride_done/$bookingId") {
                             popUpTo("payment_qr/{amount}/{bookingId}") { inclusive = true }
                        }
                    }
                }
            )
        }

        // CUSTOMER CHAT LIST
        composable(NavRoutes.CustomerChatList.route) {
            CustomerChatListScreen(navController = navController)
        }
        
        // CUSTOMER CHAT
        composable(
            route = "customer_chat/{chatId}/{contactName}/{phoneNumber}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("contactName") { type = NavType.StringType },
                navArgument("phoneNumber") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            CustomerChatScreen(
                navController = navController,
                chatId = backStackEntry.arguments?.getString("chatId") ?: "",
                contactName = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("contactName") ?: "", "UTF-8"),
                phoneNumber = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("phoneNumber") ?: "", "UTF-8")
            )
        }
        
        // DRIVER CHAT LIST
        composable(NavRoutes.DriverChatList.route) {
            DriverChatListScreen(navController = navController)
        }
        
        // DRIVER CHAT
        composable(
            route = "driver_chat/{chatId}/{contactName}/{phoneNumber}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("contactName") { type = NavType.StringType },
                navArgument("phoneNumber") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            DriverChatScreen(
                navController = navController,
                chatId = backStackEntry.arguments?.getString("chatId") ?: "",
                contactName = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("contactName") ?: "", "UTF-8"),
                phoneNumber = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("phoneNumber") ?: "", "UTF-8")
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
        // DRIVER NAVIGATION
        composable(
            route = "driver_navigation_screen/{lat}/{lng}/{address}/{bookingId}",
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType },
                navArgument("lng") { type = NavType.StringType },
                navArgument("address") { type = NavType.StringType },
                navArgument("bookingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0
            val address = backStackEntry.arguments?.getString("address") ?: ""
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            
            com.example.goukm.ui.driver.DriverNavigationScreen(
                navController = navController,
                pickupLat = lat,
                pickupLng = lng,
                pickupAddress = address,
                bookingId = bookingId
            )
        }
    }
}
