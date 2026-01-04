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
                val pickup = data["pickup"] ?: ""
                val dropOff = data["dropOff"] ?: ""
                
                NotificationHelper.showNotification(
                    applicationContext,
                    "New ride request",
                    "Yay, you have a new ride request !!"
                )
            }
            "offer_accepted" -> {
                val customerName = data["customerName"] ?: ""
                NotificationHelper.showNotification(
                    applicationContext,
                    "Your fare accepted!",
                    "Please complete the journey on time. Safe drive."
                )
            }
            "new_offer" -> {
                val driverName = data["driverName"] ?: ""
                val fare = data["fare"] ?: "0.00"
                NotificationHelper.showNotification(
                    applicationContext,
                    "Special fare offer for you!",
                    "Please check a new offer from our driver."
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
