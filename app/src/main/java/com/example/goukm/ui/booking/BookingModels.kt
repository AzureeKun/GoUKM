package com.example.goukm.ui.booking

data class DriverOffer(
    val name: String,
    val fareLabel: String, // e.g., "RM 5"
    val carBrand: String,
    val carName: String,
    val carColor: String,
    val plate: String,
    val driverId: String = "",
    val driverPhone: String = ""
)

enum class PaymentMethod {
    QR_DUITNOW,
    CASH
}
