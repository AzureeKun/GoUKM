package com.example.goukm.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

import com.example.goukm.ui.theme.CBlue
import com.example.goukm.ui.theme.AccentYellow
val LightBlueBackground = Color(0xFFE3F2FD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FareOfferScreen(
    navController: NavHostController,
    customerName: String,
    pickup: String,
    dropOff: String,
    seats: Int,
    bookingId: String
) {
    var fareAmount by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bookingRepository = remember { com.example.goukm.ui.booking.BookingRepository() }
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isWaiting by remember { mutableStateOf(false) }
    var driverProfile by remember { mutableStateOf<com.example.goukm.ui.userprofile.UserProfile?>(null) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            driverProfile = com.example.goukm.ui.userprofile.UserProfileRepository.getUserProfile(uid)
        }
    }



    // Constants for Logic
    val MIN_FARE = 3.0
    val MAX_FARE = 12.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Offer Fare",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CBlue
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title
            Text(
                "Submit Your Offer",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // Ride Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Ride Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CBlue
                    )

                    Divider(color = Color.LightGray)

                    // Customer Name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Customer",
                            tint = CBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Customer", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                customerName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Pickup Point
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Pickup",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Pickup Point", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                pickup,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Drop-off Point
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Drop-off",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Drop-off Point", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                dropOff,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Number of Seats
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightBlueBackground, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Seats Requested", fontWeight = FontWeight.Medium)
                        Text(
                            "$seats",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = CBlue
                        )
                    }
                }
            }

            // Fare Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Your Fare Offer",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CBlue
                    )

                    // Consolidated Suggested Range Info
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF9C4), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                "💡 Suggested Fare Range",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF57C00)
                            )
                            Text(
                                "RM ${String.format("%.2f", MIN_FARE)} - RM ${String.format("%.2f", MAX_FARE)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF57C00)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Fare should be reasonable based on campus distance.",
                                fontSize = 12.sp,
                                color = Color(0xFFF57C00).copy(alpha = 0.8f)
                            )
                        }
                    }

                    Divider(color = Color.LightGray)

                    // Quick Offer Buttons
                    Text(
                        "Quick Selection",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(4, 6, 8, 10).forEach { amount ->
                            val isSelected = fareAmount == amount.toString()
                            FilterChip(
                                selected = isSelected,
                                onClick = { fareAmount = amount.toString() },
                                label = { Text("RM $amount") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentYellow,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    // Fare Input
                    OutlinedTextField(
                        value = fareAmount,
                        onValueChange = {
                            fareAmount = it
                            errorMessage = null },
                        label = { Text("Enter Fare Amount") },
                        isError = errorMessage != null,
                        supportingText = {
                            if (errorMessage != null) {
                                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        leadingIcon = {
                            Text(
                                "RM",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = CBlue,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CBlue,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = CBlue
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            }

            // Submit Button with Validation Logic
            Button(
                onClick = {
                    val fareValue = fareAmount.toDoubleOrNull()

                    // VALIDATION LOGIC (Syarat)
                    when {
                        fareValue == null -> {
                            errorMessage = "Please enter a valid amount"
                        }
                        fareValue < MIN_FARE -> {
                            errorMessage = "Minimum fare for UKM is RM ${String.format("%.2f", MIN_FARE)}"
                        }
                        fareValue > MAX_FARE -> {
                            errorMessage = "Maximum fare for UKM is RM ${String.format("%.2f", MAX_FARE)}"
                        }
                        else -> {
                            if (fareAmount.isNotBlank()) {
                                val driverId = auth.currentUser?.uid
                                if (driverId != null && driverProfile != null) {
                                    scope.launch {
                                        bookingRepository.submitOffer(
                                            bookingId = bookingId,
                                            fare = fareAmount,
                                            driverId = driverId,
                                            driverName = driverProfile!!.name,
                                            vehicleType = driverProfile!!.vehicleType,
                                            vehiclePlateNumber = driverProfile!!.vehiclePlateNumber,
                                            phoneNumber = driverProfile!!.phoneNumber
                                        )
                                        // Redirect to dashboard immediately after submitting offer
                                        navController.navigate(com.example.goukm.navigation.NavRoutes.DriverDashboard.route) {
                                            popUpTo(com.example.goukm.navigation.NavRoutes.DriverDashboard.route) { inclusive = true }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentYellow,
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = fareAmount.isNotBlank()
            ) {
                Text(
                    "Submit Offer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }


    }
}
