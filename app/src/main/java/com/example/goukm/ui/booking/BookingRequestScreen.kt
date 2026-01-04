package com.example.goukm.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.goukm.navigation.NavRoutes
import com.example.goukm.ui.userprofile.CBlue
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.google.android.libraries.places.api.model.AutocompletePrediction
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.background


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingRequestScreen(navController: NavHostController, activeBookingId: String? = null) {
    var selectedSeat by remember { mutableStateOf("4-Seat") }
    
    // Autocomplete State
    var pickupQuery by remember { mutableStateOf("") }
    var pickupPredictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var pickupPlaceId by remember { mutableStateOf<String?>(null) }
    var pickupLatLng by remember { mutableStateOf<LatLng?>(null) }

    var dropOffQuery by remember { mutableStateOf("") }
    var dropOffPredictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var dropOffPlaceId by remember { mutableStateOf<String?>(null) }
    var dropOffLatLng by remember { mutableStateOf<LatLng?>(null) }
    
    // Hoisted State and Dependencies
    val context = LocalContext.current
    val placesRepository = remember { PlacesRepository(context) }
    val scope = rememberCoroutineScope()
    val bookingRepository = remember { BookingRepository() }
    var currentBookingId by remember { mutableStateOf<String?>(null) }

    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    LaunchedEffect(pickupLatLng, dropOffLatLng) {
        if (pickupLatLng != null && dropOffLatLng != null) {
            val result = placesRepository.getRoute(pickupLatLng!!, dropOffLatLng!!)
            result.onSuccess {
                routePoints = it.polyline
            }.onFailure {
                // Fallback: Draw a straight line if API fails
                routePoints = listOf(pickupLatLng!!, dropOffLatLng!!)
                android.widget.Toast.makeText(context, "Route Error: ${it.message}. Showing straight line.", android.widget.Toast.LENGTH_LONG).show()
            }
        } else {
            routePoints = emptyList()
        }
    }
    
    var isSearching by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    // Restore State if activeBookingId is present
    LaunchedEffect(activeBookingId) {
        if (!activeBookingId.isNullOrEmpty()) {
            val result = bookingRepository.getBooking(activeBookingId)
            result.onSuccess { booking ->
                currentBookingId = booking.id
                pickupQuery = booking.pickup
                dropOffQuery = booking.dropOff
                selectedSeat = if (booking.seatType.startsWith("4")) "4-Seat" else "6-Seat"
                pickupLatLng = LatLng(booking.pickupLat, booking.pickupLng)
                dropOffLatLng = LatLng(booking.dropOffLat, booking.dropOffLng)
                isSearching = true
            }.onFailure {
                android.widget.Toast.makeText(context, "Failed to load booking", android.widget.Toast.LENGTH_SHORT).show()
                isSearching = false
            }
        }
    }

    // Request permission when screen loads if not already granted
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Automatically fetch location and geocode when permission is granted
    LaunchedEffect(hasLocationPermission, currentBookingId) {
        if (hasLocationPermission && currentBookingId == null && pickupLatLng == null) {
            val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
            try {
                // Suppress missing permission warning because we checked hasLocationPermission
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        pickupLatLng = latLng
                        
                        // Reverse Geocoding
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            try {
                                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                                        if (addresses.isNotEmpty()) {
                                            val address = addresses[0]
                                            // Construct a readable address string
                                            val addressText = address.getAddressLine(0) ?: address.featureName
                                            scope.launch { 
                                                pickupQuery = addressText 
                                            }
                                        }
                                    }
                                } else {
                                    @Suppress("DEPRECATION")
                                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                    if (!addresses.isNullOrEmpty()) {
                                        val address = addresses[0]
                                        val addressText = address.getAddressLine(0) ?: address.featureName
                                        scope.launch { 
                                            pickupQuery = addressText 
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: SecurityException) {
                // Should not happen as we checked permission
            }
        }
    }

    val textFieldBg = Color(0xFFF2F3F5)
    val accentYellow = Color(0xFFFFD60A)
    val bannerBlue = Color(0xFF6B87C0)

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true // avoid fully hidden; keeps sheet draggable back up
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 160.dp,
        sheetContainerColor = Color.White,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetDragHandle = {}, // disable default handle; we draw our own
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(42.dp)
                        .height(4.dp)
                        .background(Color.LightGray, RoundedCornerShape(50))
                )

                // Status banner when searching
                if (isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bannerBlue, RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("We're finding you a driver", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                Text(
                    text = if (selectedSeat.startsWith("4")) "4 seater ride" else "6 seater ride",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                if (!isSearching) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SeatChip(
                            label = "4-Seat",
                            isSelected = selectedSeat == "4-Seat",
                            onClick = { selectedSeat = "4-Seat" }
                        )
                        Spacer(Modifier.width(12.dp))
                        SeatChip(
                            label = "6-Seat",
                            isSelected = selectedSeat == "6-Seat",
                            onClick = { selectedSeat = "6-Seat" }
                        )
                    }
                }

                if (!isSearching) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Pickup Point", fontWeight = FontWeight.Bold)
                            AutocompleteTextField(
                                label = "Enter Pickup Location",
                                value = pickupQuery,
                                onValueChange = { query ->
                                    pickupQuery = query
                                    pickupPlaceId = null // Reset validity on type
                                    pickupLatLng = null
                                    scope.launch {
                                        pickupPredictions = placesRepository.getPredictions(query)
                                    }
                                },
                                predictions = pickupPredictions,
                                onPredictionSelect = { placeId, address ->
                                    pickupQuery = address
                                    pickupPlaceId = placeId
                                    pickupPredictions = emptyList() // Hide list
                                    scope.launch {
                                        val place = placesRepository.getPlaceDetails(placeId)
                                        pickupLatLng = place?.latLng
                                    }
                                },
                                leadingIcon = Icons.Default.Send
                            )
    
                            Text("Drop-Off Point", fontWeight = FontWeight.Bold)
                            AutocompleteTextField(
                                label = "Enter Drop-off Location",
                                value = dropOffQuery,
                                onValueChange = { query ->
                                    dropOffQuery = query
                                    dropOffPlaceId = null // Reset validity
                                    dropOffLatLng = null
                                    scope.launch {
                                        dropOffPredictions = placesRepository.getPredictions(query)
                                    }
                                },
                                predictions = dropOffPredictions,
                                onPredictionSelect = { placeId, address ->
                                    dropOffQuery = address
                                    dropOffPlaceId = placeId
                                    dropOffPredictions = emptyList() // Hide list
                                    scope.launch {
                                        val place = placesRepository.getPlaceDetails(placeId)
                                        dropOffLatLng = place?.latLng
                                    }
                                },
                                leadingIcon = Icons.Default.Place
                            )
                        }
                    }
                } else {
                    // Static Trip Details when searching
                     Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FB))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                             Text("Trip Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                             
                             Row(verticalAlignment = Alignment.CenterVertically) {
                                 Icon(Icons.Default.Send, contentDescription = null, tint = CBlue, modifier = Modifier.size(20.dp))
                                 Spacer(Modifier.width(8.dp))
                                 Column {
                                     Text("Pickup", fontSize = 12.sp, color = Color.Gray)
                                     Text(pickupQuery, fontWeight = FontWeight.SemiBold)
                                 }
                             }
                             
                             androidx.compose.material3.Divider()
                             
                             Row(verticalAlignment = Alignment.CenterVertically) {
                                 Icon(Icons.Default.Place, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                                 Spacer(Modifier.width(8.dp))
                                 Column {
                                     Text("Drop-off", fontSize = 12.sp, color = Color.Gray)
                                     Text(dropOffQuery, fontWeight = FontWeight.SemiBold)
                                 }
                             }
                        }
                    }
                }

                var showValidationToast by remember { mutableStateOf(false) }

                Button(
                    onClick = {
                        if (isSearching) {
                            // Show confirmation dialog
                            showCancelDialog = true
                        } else {
                            // VALIDATION LOGIC
                            when {
                                pickupQuery.isBlank() || dropOffQuery.isBlank() -> {
                                    android.widget.Toast.makeText(context, "Please enter pickup and drop-off locations", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                pickupPlaceId == null || dropOffPlaceId == null -> {
                                    android.widget.Toast.makeText(context, "Please select locations from the suggestions", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                pickupLatLng == null || dropOffLatLng == null -> {
                                    android.widget.Toast.makeText(context, "Unable to get coordinates for selected locations", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                pickupPlaceId == dropOffPlaceId -> {
                                    android.widget.Toast.makeText(context, "Pickup and Drop-off cannot be the same", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    // All Valid
                                    scope.launch {
                                        val pickupLat = pickupLatLng?.latitude ?: 0.0
                                        val pickupLng = pickupLatLng?.longitude ?: 0.0
                                        val dropOffLat = dropOffLatLng?.latitude ?: 0.0
                                        val dropOffLng = dropOffLatLng?.longitude ?: 0.0

                                        val result = bookingRepository.createBooking(
                                            pickup = pickupQuery, 
                                            dropOff = dropOffQuery, 
                                            seatType = selectedSeat,
                                            pickupLat = pickupLat,
                                            pickupLng = pickupLng,
                                            dropOffLat = dropOffLat,
                                            dropOffLng = dropOffLng
                                        )
                                        result.onSuccess { bookingId ->
                                             currentBookingId = bookingId
                                             isSearching = true
                                             android.widget.Toast.makeText(context, "Booking Request Sent!", android.widget.Toast.LENGTH_SHORT).show()
                                        }.onFailure { e ->
                                             android.widget.Toast.makeText(context, "Failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentYellow),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isSearching) "Cancel Booking" else "Booking Ride",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        // Map area stays full screen; sheet slides over it
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // UKM Bangi coordinates (approximate center)
            val ukmLocation = LatLng(2.9300, 101.7774)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(ukmLocation, 15f)
            }
            
            // Animate camera to pickup location when auto-detected
            LaunchedEffect(pickupLatLng) {
                if (pickupLatLng != null && routePoints.isEmpty()) {
                    cameraPositionState.animate(
                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(pickupLatLng!!, 17f),
                        1000
                    )
                }
            }
             
            val mapProperties = MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = hasLocationPermission
            )
             
            val mapUiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = hasLocationPermission,
                mapToolbarEnabled = false
            )
            
            LaunchedEffect(routePoints) {
                if (routePoints.isNotEmpty()) {
                    val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.builder()
                    routePoints.forEach { boundsBuilder.include(it) }
                    val bounds = boundsBuilder.build()
                    cameraPositionState.animate(
                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(bounds, 100)
                    )
                }
            }
             
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings,
                onMapLoaded = { /* Map is ready */ }
            ) {
                if (pickupLatLng != null) {
                    Marker(
                        state = MarkerState(position = pickupLatLng!!),
                        title = "Pickup",
                        snippet = pickupQuery,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
                if (dropOffLatLng != null) {
                    Marker(
                        state = MarkerState(position = dropOffLatLng!!),
                        title = "Drop off",
                        snippet = dropOffQuery,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }
                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color = CBlue,
                        width = 16f,
                        jointType = JointType.ROUND,
                        startCap = RoundCap(),
                        endCap = RoundCap(),
                        geodesic = true
                    )
                }
            }
            
            // Cancel Confirmation Dialog
            if (showCancelDialog) {
                AlertDialog(
                    onDismissRequest = { showCancelDialog = false },
                    containerColor = Color(0xFFFFEBEE),
                    title = {
                        Text(
                            text = "Cancel Booking",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "Do you really want to cancel the booking?",
                            color = Color.Black
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                scope.launch {
                                    if (currentBookingId != null) {
                                        val result = bookingRepository.updateStatus(currentBookingId!!, BookingStatus.CANCELLED)
                                        result.onSuccess {
                                            android.widget.Toast.makeText(context, "Booking Cancelled", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    showCancelDialog = false
                                    isSearching = false
                                    currentBookingId = null
                                    
                                    // Reset Inputs and Map
                                    pickupQuery = ""
                                    dropOffQuery = ""
                                    pickupLatLng = null
                                    dropOffLatLng = null
                                    pickupPlaceId = null
                                    dropOffPlaceId = null
                                    routePoints = emptyList()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Yes", color = Color.White)
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showCancelDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("No", color = Color.White)
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .background(Color.White, androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    }
}

@Composable
private fun RowScope.SeatChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bg = if (isSelected) CBlue else Color(0xFFE0E6F3)
    val contentColor = if (isSelected) Color.White else Color.Black

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bg),
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .weight(1f)
            .height(44.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        Text(label, color = contentColor, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutocompleteTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    predictions: List<AutocompletePrediction>,
    onPredictionSelect: (String, String) -> Unit, // placeId, address
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color(0xFFF2F3F5),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (predictions.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .padding(top = 60.dp) // Below text field
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp
            ) {
                LazyColumn(
                    modifier = Modifier.background(Color.White)
                ) {
                    items(predictions.size) { index ->
                        val prediction = predictions[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPredictionSelect(prediction.placeId, prediction.getPrimaryText(null).toString())
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = prediction.getPrimaryText(null).toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = prediction.getSecondaryText(null).toString(),
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

