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
import androidx.activity.compose.BackHandler
import com.example.goukm.navigation.NavRoutes
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.Polyline
import androidx.navigation.NavHostController
import com.example.goukm.ui.booking.PlacesRepository
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerJourneyDetailsScreen(
    onChatClick: (chatId: String, name: String, phone: String) -> Unit,
    navController: NavHostController,
    paymentMethod: String,
    initialPaymentStatus: String = "PENDING"
) {
    val bookingId = navController.currentBackStackEntry?.arguments?.getString("bookingId") ?: ""
    val viewModel: CustJourneyDetailsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    LaunchedEffect(bookingId) {
        if (bookingId.isNotEmpty()) {
            viewModel.initialize(bookingId, initialPaymentStatus)
        }
    }

    // Observers
    val paymentStatus by viewModel.paymentStatus.collectAsState()
    val driverArrived by viewModel.driverArrived.collectAsState()
    val driverLatLng by viewModel.driverLatLng.collectAsState()
    val routePoints by viewModel.routePoints.collectAsState()
    
    val driverName by viewModel.driverName.collectAsState()
    val driverPhone by viewModel.driverPhone.collectAsState()
    val chatRoomId by viewModel.chatRoomId.collectAsState()
    val carBrand by viewModel.carBrand.collectAsState()
    val carModel by viewModel.carModel.collectAsState()
    val carColor by viewModel.carColor.collectAsState()
    val carPlate by viewModel.carPlate.collectAsState()
    val driverProfileUrl by viewModel.driverProfileUrl.collectAsState()
    val driverRating by viewModel.driverRating.collectAsState()
    
    val pickupAddress by viewModel.pickupAddress.collectAsState()
    val dropOffAddress by viewModel.dropOffAddress.collectAsState()
    val fareAmount by viewModel.fareAmount.collectAsState()
    val passengerName by viewModel.passengerName.collectAsState()
    val passengerProfileUrl by viewModel.passengerProfileUrl.collectAsState()
    val pickupLatLng by viewModel.pickupLatLng.collectAsState()
    val dropOffLatLng by viewModel.dropOffLatLng.collectAsState()
    
    val showArrivedAlert by viewModel.showArrivedAlert.collectAsState()
    val showDriverCancelledAlert by viewModel.showDriverCancelledAlert.collectAsState()
    val navToRating by viewModel.navToRating.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val currentPaymentStatus = navController.currentBackStackEntry?.savedStateHandle
        ?.getStateFlow("paymentStatus", initialPaymentStatus)
        ?.collectAsState()?.value ?: initialPaymentStatus

    // Navigation Logic
    BackHandler {
        navController.navigate(NavRoutes.CustomerDashboard.route) {
            popUpTo(NavRoutes.CustomerDashboard.route) { inclusive = true }
        }
    }

    LaunchedEffect(navToRating) {
        if (navToRating) {
             navController.navigate("ride_done/$bookingId") {
                popUpTo(NavRoutes.CustomerDashboard.route) { inclusive = false }
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
                    onChatClick = {
                        if (chatRoomId.isNotEmpty()) {
                            onChatClick(chatRoomId, driverName, driverPhone)
                        }
                    },
                    driverName = driverName,
                    driverPhone = driverPhone,
                    carBrand = carBrand,
                    carModel = carModel,
                    carColor = carColor,
                    carPlate = carPlate,
                    driverRating = driverRating,
                    driverProfileUrl = driverProfileUrl,
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
                    
                    // Show "Back to Home" if the trip is completed -> REMOVED as per request to auto-navigate
                    // No manual back button here anymore.

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
                    passengerName = passengerName,
                    passengerProfileUrl = passengerProfileUrl
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

                // --- Live Driver Marker ---
                driverLatLng?.let {
                    com.google.maps.android.compose.Marker(
                        state = com.google.maps.android.compose.rememberMarkerState(position = it),
                        title = "Your Driver",
                        snippet = "Current Location",
                        icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                // --- Driver-to-Destination Route Line ---
                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color = Color(0xFF6B87C0),
                        width = 12f // 5.dp roughly
                    )
                }
            }
            
            // Optional: Floating Pill "Your driver is on the way"
            if (!driverArrived) {
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
            onDismissRequest = { viewModel.dismissArrivedAlert() },
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

    // Driver Cancelled Alert
    if (showDriverCancelledAlert) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { /* Force action */ },
            title = { Text("Ride Cancelled", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text("We're sorry, but the driver has cancelled your ride request. You can try booking another ride.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissDriverCancelled()
                        navController.navigate(NavRoutes.BookingRequest.route) {
                            popUpTo(NavRoutes.CustomerDashboard.route) { inclusive = false }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B87C0))
                ) {
                    Text("Book Again", color = Color.White)
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
    driverPhone: String,
    carBrand: String,
    carModel: String,
    carColor: String,
    carPlate: String,
    driverRating: String,
    driverProfileUrl: String = "",
    fare: String
) {
    val context = androidx.compose.ui.platform.LocalContext.current
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
                    if (driverProfileUrl.isNotEmpty()) {
                        androidx.compose.foundation.Image(
                            painter = coil.compose.rememberAsyncImagePainter(driverProfileUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxSize()
                        )
                    }
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
                            text = driverRating,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            if (carBrand.isNotEmpty()) append(carBrand)
                            if (carModel.isNotEmpty()) {
                                if (isNotEmpty()) append(" ")
                                append(carModel)
                            }
                            if (carColor.isNotEmpty()) {
                                append(" ($carColor)")
                            }
                            if (isEmpty()) append("Car Details")
                        },
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
                    onClick = {
                        if (driverPhone.isNotEmpty()) {
                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:$driverPhone"))
                            context.startActivity(intent)
                        }
                    },
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
fun JourneySummarySection(
    pickup: String, 
    dropOff: String, 
    passengerName: String,
    passengerProfileUrl: String = ""
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        // Passenger Info Row (Optional based on image, but adds "Professional" touch)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = Color.LightGray,
                modifier = Modifier.size(40.dp)
            ) {
                 if (passengerProfileUrl.isNotEmpty()) {
                    androidx.compose.foundation.Image(
                        painter = coil.compose.rememberAsyncImagePainter(passengerProfileUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                 } else {
                     Icon(
                         Icons.Default.Person,
                         contentDescription = null,
                         tint = Color.Gray,
                         modifier = Modifier.padding(8.dp)
                     )
                 }
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
