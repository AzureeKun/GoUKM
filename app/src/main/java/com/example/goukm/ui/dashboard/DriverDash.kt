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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.goukm.R
import com.example.goukm.booking.RideRequestCard
import com.example.goukm.booking.RideRequestModel
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

    // ✅ DUMMY DATA UNTUK TEST
    val rideRequests = listOf(
        RideRequestModel(
            customerName = "Aisyah",
            pickupPoint = "Block E",
            dropOffPoint = "FST",
            seats = 4,
            requestedTimeAgo = "2 min ago",
            customerImageRes = R.drawable.ic_account_circle_24
        ),
        RideRequestModel(
            customerName = "Amir",
            pickupPoint = "FTSM",
            dropOffPoint = "Kolej Pendeta",
            seats = 2,
            requestedTimeAgo = "5 min ago",
            customerImageRes = R.drawable.ic_account_circle_24
        )
    )

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
                        isOnline = it      // ✅ LOCAL CHANGE
                        // onToggleStatus() // ❌ NANTI dulu (data sebenar)
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
