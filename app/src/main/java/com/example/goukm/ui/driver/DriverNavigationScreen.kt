package com.example.goukm.ui.driver

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.example.goukm.ui.booking.PlacesRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import com.example.goukm.ui.theme.CBlue

enum class NavMode { TO_PICKUP, TO_DROPOFF }

@Composable
fun DriverNavigationScreen(
    navController: NavHostController,
    pickupLat: Double,
    pickupLng: Double,
    pickupAddress: String,
    bookingId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val placesRepository = remember { PlacesRepository(context) }
    val bookingRepository = remember { com.example.goukm.ui.booking.BookingRepository() }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // State
    var booking by remember { mutableStateOf<com.example.goukm.ui.booking.Booking?>(null) }
    var navMode by remember { mutableStateOf(NavMode.TO_PICKUP) }
    var destinationLocation by remember { mutableStateOf(LatLng(pickupLat, pickupLng)) }
    var destinationAddress by remember { mutableStateOf(pickupAddress) }
    var showPaymentPendingAlert by remember { mutableStateOf(false) }

    // Monitor Booking Details in Real-Time
    LaunchedEffect(bookingId) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val docRef = db.collection("bookings").document(bookingId)
        val registration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
            
            val fetchedBooking = snapshot.toObject(com.example.goukm.ui.booking.Booking::class.java)?.copy(id = snapshot.id)
            if (fetchedBooking != null) {
                booking = fetchedBooking
                // Adjust navMode if booking is already ONGOING or higher
                val status = fetchedBooking.status
                if (status == com.example.goukm.ui.booking.BookingStatus.ONGOING.name || 
                    fetchedBooking.driverArrived || 
                    status == com.example.goukm.ui.booking.BookingStatus.COMPLETED.name) {
                    navMode = NavMode.TO_DROPOFF
                }
            }
        }
    }

    // Update destination when NavMode changes or booking updates
    LaunchedEffect(navMode, booking) {
        booking?.let {
            if (navMode == NavMode.TO_DROPOFF) {
                destinationLocation = LatLng(it.dropOffLat, it.dropOffLng)
                destinationAddress = it.dropOff
            } else {
                destinationLocation = LatLng(it.pickupLat, it.pickupLng)
                destinationAddress = it.pickup
            }
        }
    }

    // Fallback: Geocode if Lat/Lng is still 0.0 (e.g. initial params or freshly fetched booking)
    LaunchedEffect(destinationLocation, destinationAddress) {
        if (destinationLocation.latitude == 0.0 && destinationLocation.longitude == 0.0 && destinationAddress.isNotEmpty()) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val geocoder = android.location.Geocoder(context)
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(destinationAddress, 1)
                    if (!addresses.isNullOrEmpty()) {
                        destinationLocation = LatLng(addresses[0].latitude, addresses[0].longitude)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    var driverLocation by remember { mutableStateOf<LatLng?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var distanceToDestination by remember { mutableStateOf("") }
    var isMapReady by remember { mutableStateOf(false) }

    // Camera state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(destinationLocation, 15f)
    }

    // Permission handling
    var hasLocationPermission by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Location Updates
    DisposableEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 2000
            ).build()

            val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                    val location = result.lastLocation ?: return
                    val latLng = LatLng(location.latitude, location.longitude)
                    driverLocation = latLng

                    // Animate camera to follow driver with 3D tilt
                    if (isMapReady) {
                        val cameraUpdate = CameraPosition.Builder()
                            .target(latLng)
                            .zoom(18f)
                            .bearing(location.bearing) // Rotate based on driving direction
                            .tilt(45f) // 3D effect
                            .build()
                        scope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(cameraUpdate), 1000)
                        }
                    }
                }
            }

            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    android.os.Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                // Handle exception
            }

            onDispose {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        } else {
            onDispose { }
        }
    }

    // Calculate Route when Driver Location is first found or mode changes
    LaunchedEffect(driverLocation, destinationLocation) {
        if (driverLocation != null) {
            val result = placesRepository.getRoute(driverLocation!!, destinationLocation)
            result.onSuccess {
                routePoints = it.polyline
                distanceToDestination = it.distance
            }.onFailure {
                 // Silent fail or toast
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapType = com.google.maps.android.compose.MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                compassEnabled = true,
                zoomControlsEnabled = false // Hide default zoom for cleaner nav UI
            ),
            onMapLoaded = { isMapReady = true }
        ) {
            // Destination Marker
            Marker(
                state = MarkerState(position = destinationLocation),
                title = if (navMode == NavMode.TO_PICKUP) "Pickup Point" else "Drop-off Point",
                snippet = destinationAddress
            )

            // Route Polyline
            if (routePoints.isNotEmpty()) {
                Polyline(
                    points = routePoints,
                    color = if (navMode == NavMode.TO_PICKUP) Color(0xFF4285F4) else Color(0xFF1976D2), 
                    width = 20f,
                    geodesic = true
                )
            }
        }

        // Top Info Card (Instruction)
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (navMode == NavMode.TO_PICKUP) "Navigating to Pickup" else "Navigating to Drop-off",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    if (distanceToDestination.isNotEmpty()) {
                        Text(
                            text = distanceToDestination,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4285F4)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = destinationAddress,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
            }
        }

        // Bottom Action (Buttons)
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Navigate External Button
                Button(
                    onClick = {
                        val gmmIntentUri = android.net.Uri.parse("google.navigation:q=${destinationLocation.latitude},${destinationLocation.longitude}")
                        val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        try {
                             context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Google Maps not found", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                     Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White)
                     Spacer(modifier = Modifier.padding(4.dp))
                     Text("Navigate (Maps)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                // Help/GPS Status
                if (driverLocation == null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Acquiring GPS...", color = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    }
                }

                // Primary Action Button
                if (navMode == NavMode.TO_PICKUP) {
                    Button(
                        onClick = {
                            scope.launch {
                                bookingRepository.updateDriverArrived(bookingId)
                                bookingRepository.updateStatus(bookingId, com.example.goukm.ui.booking.BookingStatus.ONGOING)
                                navMode = NavMode.TO_DROPOFF
                                routePoints = emptyList() // Trigger recalculation
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("I Have Arrived", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            if (booking?.paymentStatus == "PAID") {
                                scope.launch {
                                    bookingRepository.updateStatus(bookingId, com.example.goukm.ui.booking.BookingStatus.COMPLETED)
                                    navController.navigate("driver_journey_summary/$bookingId") {
                                        popUpTo("driver_navigation_screen") { inclusive = true }
                                    }
                                }
                            } else {
                                showPaymentPendingAlert = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Complete Trip", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Payment Pending Alert
        if (showPaymentPendingAlert) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showPaymentPendingAlert = false },
                title = { Text("Payment Pending", fontWeight = FontWeight.Bold) },
                text = { Text("Customer has not paid yet. Please wait for the passenger to complete the payment.") },
                confirmButton = {
                    Button(
                        onClick = { showPaymentPendingAlert = false },
                        colors = ButtonDefaults.buttonColors(containerColor = CBlue)
                    ) {
                        Text("Okay", color = Color.White)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
