package com.example.goukm.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.goukm.R
import com.example.goukm.booking.RideRequestCard
import com.example.goukm.booking.RideRequestModel
import com.example.goukm.ui.history.DriverRideBookingHistoryScreen
import com.example.goukm.ui.register.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun DriverDashboard(
            navController: NavHostController,
    authViewModel: AuthViewModel,

    /*ADA FIREBASE BARU GUNA
    isOnline: Boolean,
    rideRequests: List<RideRequestModel>,
    onToggleStatus: () -> Unit,*/
    selectedNavIndex: Int,
    onNavSelected: (Int) -> Unit
) {
    // ✅ ADD STATE DALAM NI (SENANG TEST)
    var isOnline by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
         val profile = com.example.goukm.ui.userprofile.UserProfileRepository.getUserProfile()
         if (profile != null) {
             isOnline = profile.isAvailable
         }

         // Get FCM Token
         com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
             if (!task.isSuccessful) {
                 return@addOnCompleteListener
             }
             val token = task.result
             scope.launch {
                 com.example.goukm.ui.userprofile.UserProfileRepository.saveFCMToken(token)
             }
         }
    }

    // ✅ Real-time Data from Firestore
    var rideRequests by remember { mutableStateOf<List<RideRequestModel>>(emptyList()) }

    DisposableEffect(isOnline) {
        if (isOnline) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val listener = db.collection("bookings")
                .whereEqualTo("status", "PENDING")
                .addSnapshotListener { snapshots, e ->
                    if (e != null || snapshots == null) return@addSnapshotListener

                    scope.launch {
                        val newRequests = snapshots.documents.mapNotNull { doc ->
                            val pickup = doc.getString("pickup") ?: ""
                            val dropOff = doc.getString("dropOff") ?: ""
                            val seatType = doc.getString("seatType") ?: "4"
                            val seats = seatType.filter { it.isDigit() }.toIntOrNull() ?: 4
                            val userId = doc.getString("userId") ?: ""
                            val timestamp = doc.getDate("timestamp")
                            
                            val timeAgo = if (timestamp != null) {
                                val diff = java.util.Date().time - timestamp.time
                                val min = diff / 60000
                                if (min < 1) "Just now" else "$min min ago"
                            } else "Just now"

                            // Fetch user profile for name
                            val userProfile = com.example.goukm.ui.userprofile.UserProfileRepository.getUserProfile(userId)
                            val name = userProfile?.name ?: "Passenger"
                            
                            // Note: RideRequestModel currently uses Int for image arg.
                            RideRequestModel(
                                customerName = name,
                                pickupPoint = pickup,
                                dropOffPoint = dropOff,
                                seats = seats,
                                requestedTimeAgo = timeAgo,
                                customerImageRes = R.drawable.ic_account_circle_24
                            )
                        }
                        rideRequests = newRequests
                    }
                }
            onDispose { listener.remove() }
        } else {
            rideRequests = emptyList()
            onDispose { }
        }
    }

    Scaffold(
        containerColor = Color(0xFFB0BAC8),

        // ✅ Bottom bar fixed kat bawah
        bottomBar = {
            BottomNavigationBarDriver(
                selectedIndex = selectedNavIndex,
                onSelected = onNavSelected
            )
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // ✅ Top bar: Online / Offline
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Switch(
                    checked = isOnline,
                    onCheckedChange = {
                        isOnline = it
                        scope.launch {
                             com.example.goukm.ui.userprofile.UserProfileRepository.updateDriverAvailability(it)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Green,
                        uncheckedThumbColor = Color.Red
                    )
                )

                Text(
                    text = if (isOnline) "Online" else "Offline",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .background(
                            color = if (isOnline) Color.Green else Color.Red,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    color = Color.White
                )

                Spacer(modifier = Modifier.weight(1f))

                // Chat Button
                IconButton(onClick = { navController.navigate("driver_chat_list") }) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "Chat",
                        tint = Color.White
                    )
                }

                IconButton(onClick = { /* TODO: Settings */ }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }

            if (isOnline) {

                Text(
                    text = "Nearest Requests",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(rideRequests) { request ->
                        RideRequestCard(
                            request = request,
                            onSkip = { },
                            onOffer = {
                                navController.navigate(
                                    "fare_offer/${request.customerName}/${request.pickupPoint}/${request.dropOffPoint}/${request.seats}"
                                )
                            }
                        )
                    }
                }

            } else {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Image(
                            painter = painterResource(id = R.drawable.carroad),
                            contentDescription = "No Request",
                            modifier = Modifier.size(180.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "No pending request",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            "Switch to online to get ride request",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                // Switch driver → customer
                                scope.launch {
                                    authViewModel.switchActiveRole("customer") // persists role in SessionManager
                                    navController.navigate("customer_dashboard") {
                                        popUpTo("driver_dashboard") { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Text("Switch to Customer Mode")
                        }
                        // Logout Button
                        Button(
                            onClick = {
                                authViewModel.logout()
                                navController.navigate("register") {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Logout", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}


