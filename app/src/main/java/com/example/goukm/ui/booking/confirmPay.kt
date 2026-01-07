package com.example.goukm.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun confirmPay(
    bookingId: String,
    paymentMethod: String, // "CASH" or "QR_DUITNOW"
    onBack: () -> Unit = {},
    onProceedPayment: () -> Unit = {}, 
    oncashConfirmation: () -> Unit = {},
    onNavigateToQR: (String) -> Unit = {} // Updated to pass amount
) {
    var showCashDialog by remember { mutableStateOf(false) }
    
    // State for fetched data
    var isLoading by remember { mutableStateOf(true) }
    var pickup by remember { mutableStateOf("") }
    var dropOff by remember { mutableStateOf("") }
    var seatType by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("0.00") }
    var driverName by remember { mutableStateOf("Driver") }
    // Split name for display like original mock (optional, but good for style)
    var driverFirstName by remember { mutableStateOf("") }
    var driverLastName by remember { mutableStateOf("") }
    
    var carBrand by remember { mutableStateOf("-") }
    var carName by remember { mutableStateOf("-") } // Usually Model
    var carColor by remember { mutableStateOf("-") }
    var carPlate by remember { mutableStateOf("-") }
    
    LaunchedEffect(bookingId) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        try {
            val bookingSnapshot = db.collection("bookings").document(bookingId).get().await()
            if (bookingSnapshot.exists()) {
                pickup = bookingSnapshot.getString("pickup") ?: ""
                dropOff = bookingSnapshot.getString("dropOff") ?: ""
                seatType = bookingSnapshot.getString("seatType") ?: "4"
                totalAmount = bookingSnapshot.get("offeredFare")?.toString() ?: "0.00"
                
                val driverId = bookingSnapshot.getString("driverId")
                if (!driverId.isNullOrEmpty()) {
                    val driverSnapshot = db.collection("users").document(driverId).get().await()
                    val fullName = driverSnapshot.getString("name") ?: "Driver"
                    val parts = fullName.split(" ")
                    if (parts.isNotEmpty()) {
                         driverFirstName = parts[0].uppercase()
                         driverLastName = if (parts.size > 1) parts.drop(1).joinToString(" ").uppercase() else ""
                    } else {
                        driverFirstName = fullName.uppercase()
                    }
                    
                    carBrand = driverSnapshot.getString("carBrand") ?: ""
                    carName = driverSnapshot.getString("vehicleType") ?: "" // mapped to vehicleType often
                    carColor = driverSnapshot.getString("carColor") ?: ""
                    carPlate = driverSnapshot.getString("vehiclePlateNumber") ?: ""
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Booking Details",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 36.sp,
                )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF6B87C0)
                )
            )
        },
        containerColor = Color(0xFFE1F5FE),
        bottomBar = {
            if (!isLoading) {
                Button(
                    onClick = {
                        if (paymentMethod == "CASH") {
                            showCashDialog = true
                        } else {
                            onNavigateToQR(totalAmount)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 20.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Text(
                        "Proceed Payment",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6B87C0),
                            Color(0xFFE3F2FD),
                            Color(0xFFE1F5FE)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Seat Info
                            Text(
                                "Seat: ${seatType} seater",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF1976D2),
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Pickup Location
                            LocationItem(
                                pickup,
                                Icons.Default.ArrowUpward,
                                true
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Dropoff Location
                            LocationItem(
                                dropOff,
                                Icons.Default.ArrowDownward,
                                false
                            )
                            Spacer(modifier = Modifier.height(28.dp))

                            // Driver & Car Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Profile Placeholder
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(Color.Black, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                     // Could add driver Image here
                                     Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                }

                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        driverFirstName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    if (driverLastName.isNotEmpty()) {
                                        Text(
                                            driverLastName,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // Car Details
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Car: $carBrand", fontSize = 14.sp)
                                    Text("Model: $carName", fontSize = 14.sp)
                                    Text("Color: $carColor", fontSize = 14.sp)
                                    Text("Plate: $carPlate", fontSize = 14.sp)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Total Price row
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFD700), RoundedCornerShape(16.dp))
                                        .padding(horizontal = 20.dp, vertical = 16.dp)
                                ) {
                                    Text(
                                        "RM $totalAmount",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    if (showCashDialog) {
        AlertDialog(
            onDismissRequest = { showCashDialog = false },
            title = { Text("Cash Payment Reminder") },
            text = { Text("Please pay RM $totalAmount directly to the driver upon completion of the ride.") },
            confirmButton = {
                Button(
                    onClick = {
                        showCashDialog = false
                        oncashConfirmation()
                    }
                ) {
                    Text("I Understand")
                }
            },
            dismissButton = {
                Button(onClick = { showCashDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LocationItem(
    location: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPickup: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .background(
                    if (isPickup) Color(0xFF4CAF50) else Color(0xFFF44336),
                    CircleShape
                )
                .padding(6.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                location,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Text(
                if (isPickup) "Pickup Location" else "Dropoff Location",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
