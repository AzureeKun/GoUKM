package com.example.goukm.booking

data class RideRequestModel(
    val customerImageRes: Int,
    val customerName: String,
    val pickupPoint: String,
    val dropOffPoint: String,
    val seats: Int,
    val requestedTimeAgo: String, // ex: "Just now", "5 min ago"
    val id: String = "",
    val offeredFare: String = "",
    val pickupLat: Double = 0.0,
    val pickupLng: Double = 0.0,
    val chatRoom: com.example.goukm.ui.chat.ChatRoom? = null,
    val driverArrived: Boolean = false
)
