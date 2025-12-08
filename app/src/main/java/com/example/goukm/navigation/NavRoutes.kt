package com.example.goukm.navigation

sealed class NavRoutes(val route: String) {
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    object NamePage : NavRoutes("name_page")
    object RegisterOption : NavRoutes("register_option")

    object CustomerDashboard : NavRoutes("customer_dashboard")
    object DriverDashboard : NavRoutes("driver_dashboard")
    object BookingRequest : NavRoutes("booking_request")

    object CustomerProfile : NavRoutes("customer_profile")
    object EditProfile : NavRoutes("edit_profile")
    object DriverApplication : NavRoutes("driver_application")
    object Loading : NavRoutes("loading")
    object verificationIC : NavRoutes("verification_ic")
    object verificationDocuments : NavRoutes("verification_doc")


}
