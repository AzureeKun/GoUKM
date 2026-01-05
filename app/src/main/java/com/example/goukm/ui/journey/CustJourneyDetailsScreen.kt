package com.example.goukm.ui.journey

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.goukm.navigation.NavRoutes
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.navigation.NavHostController



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerJourneyDetailsScreen(
    onChatClick: () -> Unit,
    navController: NavHostController,
    paymentMethod: String,
    initialPaymentStatus: String = "PENDING" // Passed from nav or backend
) {
    val bookingId = navController.currentBackStackEntry?.arguments?.getString("bookingId") ?: ""
    var paymentStatus by remember { mutableStateOf(initialPaymentStatus) }
    var showArrivedAlert by remember { mutableStateOf(false) }
    var isDriverArrived by remember { mutableStateOf(false) }
    
    val currentPaymentStatus = navController.currentBackStackEntry?.savedStateHandle
        ?.getStateFlow("paymentStatus", initialPaymentStatus)
        ?.collectAsState()?.value ?: initialPaymentStatus

    // MONITOR BOOKING STATUS FOR COMPLETION
    LaunchedEffect(bookingId) {
        if (bookingId.isNotEmpty()) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val docRef = db.collection("bookings").document(bookingId)
            val registration = docRef.addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                
                val status = snapshot.getString("status")
                val pStatus = snapshot.getString("paymentStatus") ?: "PENDING"
                val arrived = snapshot.getBoolean("driverArrived") ?: false
                
                paymentStatus = pStatus
                isDriverArrived = arrived

                if (status == "COMPLETED") {
                    if (pStatus == "PAID") {
                        navController.navigate("ride_done") {
                            popUpTo("cust_journey_details/$bookingId") { inclusive = true }
                        }
                    } else {
                        showArrivedAlert = true
                    }
                }
            }
        }
    }

    LaunchedEffect(currentPaymentStatus) {
        if (currentPaymentStatus == "PAID") {
             paymentStatus = "PAID"
        }
    }

    val bookingRepository = remember { com.example.goukm.ui.booking.BookingRepository() }
    var driverName by remember { mutableStateOf("Driver") }
    var carModel by remember { mutableStateOf("Car Model") }
    var carPlate by remember { mutableStateOf("Plate") }
    var pickupAddress by remember { mutableStateOf("Loading...") }
    var dropOffAddress by remember { mutableStateOf("Loading...") }
    var fareAmount by remember { mutableStateOf("...") }
    var passengerName by remember { mutableStateOf("Passenger") }
    var pickupLatLng by remember { mutableStateOf<LatLng?>(null) }
    var dropOffLatLng by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(bookingId) {
        if (bookingId.isNotEmpty()) {
            val result = bookingRepository.getBooking(bookingId)
            val booking = result.getOrNull()
            if (booking != null) {
                pickupAddress = booking.pickup
                dropOffAddress = booking.dropOff
                fareAmount = "RM ${booking.offeredFare}"
                pickupLatLng = LatLng(booking.pickupLat, booking.pickupLng)
                dropOffLatLng = LatLng(booking.dropOffLat, booking.dropOffLng)

                val customerProfile = com.example.goukm.ui.userprofile.UserProfileRepository.getUserProfile(booking.userId)
                if (customerProfile != null) {
                    passengerName = customerProfile.name
                }

                if (!booking.driverId.isNullOrEmpty()) {
                    val userProfile = com.example.goukm.ui.userprofile.UserProfileRepository.getUserProfile(booking.driverId)
                    if (userProfile != null) {
                        driverName = userProfile.name
                        carModel = userProfile.vehicleType
                        carPlate = userProfile.vehiclePlateNumber
                    }
                }
            }
        }
    }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = true
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 200.dp,
        sheetContainerColor = Color.White,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(6.dp)
                            .background(Color.LightGray.copy(alpha = 0.4f), CircleShape)
                    )
                }

                // --- 1. Driver Details Section ---
                Text(
                    text = "Driver Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                DriverInfoCard(
                    onChatClick = onChatClick,
                    driverName = driverName,
                    carModel = carModel,
                    carPlate = carPlate,
                    fare = fareAmount
                )

                Divider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 1.dp)

                if (paymentStatus == "PAID") {
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            disabledContainerColor = Color.Gray
                        ),
                        enabled = false
                    ) {
                        Text(
                            text = "Paid",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else { 
                    // --- NEW: Proceed to Payment Button ---
                    Button(
                        onClick = {
                            navController.navigate("confirm_pay/$paymentMethod/$bookingId")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text(
                            text = "Proceed to Payment",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Divider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 1.dp)

                // --- 2. Journey Summary Section ---
                Text(
                    text = "Journey Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                JourneySummarySection(
                    pickup = pickupAddress,
                    dropOff = dropOffAddress,
                    passengerName = passengerName
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    ) { innerPadding ->
        // Map Background
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val ukmLocation = LatLng(2.9300, 101.7774)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(ukmLocation, 14f)
            }
            
            LaunchedEffect(pickupLatLng, dropOffLatLng) {
                if (pickupLatLng != null) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(pickupLatLng!!, 14f)
                }
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = false
                )
            ) {
                pickupLatLng?.let {
                    com.google.maps.android.compose.Marker(
                        state = com.google.maps.android.compose.rememberMarkerState(position = it),
                        title = "Pickup",
                        snippet = pickupAddress
                    )
                }
                dropOffLatLng?.let {
                    com.google.maps.android.compose.Marker(
                        state = com.google.maps.android.compose.rememberMarkerState(position = it),
                        title = "Drop-off",
                        snippet = dropOffAddress
                    )
                }
            }
            
            // Optional: Floating Pill "Your driver is on the way"
            if (!isDriverArrived) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 40.dp)
                        .background(Color(0xFF6B87C0), RoundedCornerShape(50))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Your driver is on the way",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Trip Finished Alert
    if (showArrivedAlert && paymentStatus != "PAID") {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { /* Don't allow dismissal if not paid */ },
            title = { Text("Trip Finished!", fontWeight = FontWeight.Bold) },
            text = { Text("Your driver has arrived at the destination. Please complete your payment to finish the trip.") },
            confirmButton = {
                Button(
                    onClick = {
                        navController.navigate("confirm_pay/$paymentMethod/$bookingId")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Pay Now", color = Color.White)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun DriverInfoCard(
    onChatClick: () -> Unit,
    driverName: String,
    carModel: String,
    carPlate: String,
    fare: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Row: Profile + Info + Price
            Row(
                verticalAlignment = Alignment.Top
            ) {
                // Avatar
                Surface(
                    shape = CircleShape,
                    color = Color.Black,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Name & Car
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = driverName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star, 
                            contentDescription = null, 
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "4.9",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = carModel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = carPlate,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }

                // Price Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF6B87C0)
                ) {
                    Text(
                        text = fare,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Buttons: Chat & Call
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Call Button
                OutlinedButton(
                    onClick = { /* Call Action */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF2E7D32)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call", fontWeight = FontWeight.Bold)
                }

                // Chat Button
                Button(
                    onClick = onChatClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6B87C0)
                    )
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun JourneySummarySection(pickup: String, dropOff: String, passengerName: String) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        // Passenger Info Row (Optional based on image, but adds "Professional" touch)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = Color.LightGray,
                modifier = Modifier.size(40.dp)
            ) {
                 Icon(
                     Icons.Default.Person,
                     contentDescription = null,
                     tint = Color.Gray,
                     modifier = Modifier.padding(8.dp)
                 )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = passengerName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Passenger",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
        }

        // Pickup
        LocationCard(
            label = "Pickup Point",
            location = pickup,
            backgroundColor = Color(0xFF6B87C0) // Matches Blue in image
        )

        // Dropoff
        LocationCard(
            label = "Drop-Off",
            location = dropOff,
            backgroundColor = Color(0xFF6B87C0) // Matches Blue in image
        )
    }
}

@Composable
fun LocationCard(
    label: String,
    location: String,
    backgroundColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .padding(vertical = 20.dp, horizontal = 16.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = location,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomerJourneyDetailsScreenPreview() {
    MaterialTheme {
        // Mock navController not really possible in preview without wrappers or mock obj, usually we just pass nulls or unsafe dummies for preview if not using it
        // Or better, just don't invoke it.
        // For compilation fix:
        // CustomerJourneyDetailsScreen(onChatClick = {}, navController = rememberNavController(), paymentMethod = "CASH")
    }
}
