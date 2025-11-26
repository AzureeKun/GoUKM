package com.example.goukm.navigation

sealed class NavRoutes(val route: String) {
    object Register : NavRoutes("register")
    object NamePage : NavRoutes("name_page")
    object RegisterOption : NavRoutes("register_option")

    object CustomerDashboard : NavRoutes("customer_dashboard")
    object DriverDashboard : NavRoutes("driver_dashboard")

    object CustomerProfile : NavRoutes("customer_profile")
}
