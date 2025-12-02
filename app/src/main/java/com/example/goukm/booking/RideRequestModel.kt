package com.example.goukm.booking

data class RideRequestModel(
    val customerImageRes: Int? = null,
    val customerName: String,
    val pickupPoint: String,
    val dropOffPoint: String,
    val seats: Int,
    val requestedTimeAgo: String // ex: "Just now", "5 min ago"
)

