package com.example.goukm.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.goukm.ui.userprofile.UserProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.goukm.util.NotificationHelper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        
        // Save the new token to Firestore
        // We use a coroutine because saving to Firestore is a suspend function or async
        CoroutineScope(Dispatchers.IO).launch {
             UserProfileRepository.saveFCMToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Ensure notification channel exists
        NotificationHelper.createNotificationChannel(applicationContext)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            NotificationHelper.showNotification(
                applicationContext,
                it.title ?: "New Message",
                it.body ?: ""
            )
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        
        when (type) {
            "new_ride_request" -> {
                val pickup = data["pickup"] ?: "Unknown"
                val dropOff = data["dropOff"] ?: "Unknown"
                // Ideally, we might want to deep link to the specific request
                NotificationHelper.showNotification(
                    applicationContext,
                    "New Ride Request",
                    "Pickup: $pickup\nDrop-off: $dropOff"
                )
            }
            "offer_accepted" -> {
                val customerName = data["customerName"] ?: "A customer"
                NotificationHelper.showNotification(
                    applicationContext,
                    "Offer Accepted!",
                    "$customerName accepted your fare offer."
                )
            }
            "new_offer" -> {
                val driverName = data["driverName"] ?: "A driver"
                val fare = data["fare"] ?: "0.00"
                NotificationHelper.showNotification(
                    applicationContext,
                    "New Ride Offer",
                    "$driverName offered RM $fare"
                )
            }
            else -> {
                // Generic handling or ignore
                val title = data["title"]
                val body = data["body"]
                if (title != null && body != null) {
                   NotificationHelper.showNotification(applicationContext, title, body)
                }
            }
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
