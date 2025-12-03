    package com.example.goukm.navigation

    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.wrapContentSize
    import androidx.compose.material3.CircularProgressIndicator
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
    import com.example.goukm.ui.form.DriverApplicationFormScreen
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.setValue
    import androidx.compose.runtime.collectAsState
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.platform.LocalContext
    import androidx.lifecycle.viewmodel.compose.viewModel
    import com.example.goukm.ui.register.AuthState
    import androidx.compose.runtime.rememberCoroutineScope
    import kotlinx.coroutines.launch

    @Composable
    fun AppNavGraph(
        navController: NavHostController,
        authViewModel: AuthViewModel = viewModel(
            factory = AuthViewModelFactory(LocalContext.current)
        )
    ) {
        val scope = rememberCoroutineScope()

        var currentUser by remember {
            mutableStateOf<UserProfile?>(null)
        }

        var selectedDriverNavIndex by remember { mutableStateOf(0) }

        val authState by authViewModel.authState.collectAsState()

        val startDestination: String = when (authState) {
            AuthState.Loading -> NavRoutes.Loading.route // You need to define a loading screen route
            AuthState.LoggedIn -> {
                // TODO: In a real app, you would fetch the role (driver/customer)
                // from the ViewModel here to decide between DriverDashboard/CustomerDashboard.
                // For now, let's default to a dashboard.
                // Assuming default logged-in view is CustomerDashboard for simplicity:
                NavRoutes.CustomerDashboard.route
            }
            AuthState.LoggedOut -> NavRoutes.Register.route
        }

        if (authState == AuthState.Loading) {
            // This prevents the NavHost from building until the session check is complete
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
            )
            return
        }

        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {

            //REGISTER
            composable(NavRoutes.Register.route) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    modifier = Modifier,
                    onNavigateToName = {
                        navController.navigate(NavRoutes.NamePage.route)
                    },
                    onLoginSuccess = { role ->
                        val destination = if (role == "customer")
                            NavRoutes.CustomerDashboard.route
                        else
                            NavRoutes.DriverDashboard.route

                        navController.navigate(destination) {
                            popUpTo(NavRoutes.Register.route) { inclusive = true }
                        }

                        // 🔑 ACTION: Fetch and Set Current User Data after successful login
                        scope.launch {
                            // TODO: Implement fetchUserProfile() in AuthViewModel/Repository
                            // currentUser = authViewModel.fetchUserProfile()
                        }
                    }
                )
            }

            //NAME PAGE
            composable(NavRoutes.NamePage.route) {
                NamePage(
                    onNavigateToRolePage = {
                        navController.navigate(NavRoutes.RegisterOption.route)
                    }
                )
            }

            //REGISTER OPTION
            composable(NavRoutes.RegisterOption.route) {
                RegisterOption(
                    onRegisterSuccess = { role ->
                        if (role == "customer")
                            navController.navigate(NavRoutes.CustomerDashboard.route)
                        else
                            navController.navigate(NavRoutes.DriverDashboard.route)

                        // 🔑 ACTION: Fetch and Set Current User Data after registration
                        scope.launch {
                            // TODO: Implement fetchUserProfile() in AuthViewModel/Repository
                            // currentUser = authViewModel.fetchUserProfile()
                        }
                    }
                )
            }

            //CUSTOMER DASHBOARD
            composable(NavRoutes.CustomerDashboard.route) {
                CustomerDashboard(navController)
            }

            //DRIVER DASHBOARD
            composable(NavRoutes.DriverDashboard.route) {
                var localSelectedDriverNavIndex by remember { mutableStateOf(0) }
                DriverDashboard(
                    navController = navController,
                    onSkip = { request -> println("Driver skipped ride: ${request.customerName}") },
                    onOffer = { request -> println("Driver offered ride: ${request.customerName}") },
                    selectedNavIndex = localSelectedDriverNavIndex,
                    onNavSelected = { index -> localSelectedDriverNavIndex = index }
                )
            }

            //CUSTOMER PROFILE
            composable(NavRoutes.CustomerProfile.route) {
                val currentUser by authViewModel.currentUser.collectAsState()
                CustomerProfileScreen(
                    navController = navController,
                    user = currentUser,
                    onEditProfile = { navController.navigate(NavRoutes.EditProfile.route) },
                    onLogout = { // COMPLETE LOGOUT LOGIC
                        // 1. Clear the authentication session
                        authViewModel.logout()

                        // 2. Clear USER profile data
                        authViewModel.clearUser()

                        // 3. Navigate back to the Register screen and clear all history
                        navController.navigate(NavRoutes.Register.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
            }

            //EDIT PROFILE
            composable(NavRoutes.EditProfile.route) {
                val currentUser by authViewModel.currentUser.collectAsState()
                // Check if user data is available before navigating to the Edit screen
                currentUser?.let { user ->
                    EditProfileScreen(
                        navController = navController,
                        user = user,
                        onSave = { updatedUser ->
                            authViewModel.updateUserProfile(updatedUser) // Update the state after save
                            navController.navigate(NavRoutes.CustomerProfile.route) {
                                popUpTo(NavRoutes.CustomerProfile.route) { inclusive = false }
                            }

                        }
                    )
                } ?: CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
                ) // Show loading if user data is missing //
            }

            //FORM APPLICATION
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

            //LOADING
            composable(NavRoutes.Loading.route) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
                )
            }

        }
    }
