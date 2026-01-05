package com.example.goukm.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.example.goukm.ui.theme.CBlue
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.libraries.places.api.model.Place



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingRequestScreen(navController: NavHostController, activeBookingId: String? = null) {
    var selectedSeat by remember { mutableStateOf("4-Seat") }
    // Payment Method State
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }

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
    var rideOffers by remember { mutableStateOf<List<DriverOffer>>(emptyList()) }

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

    // Restore State + Watch for Updates
    val effectiveBookingId = if (!activeBookingId.isNullOrEmpty()) activeBookingId else currentBookingId
    
    LaunchedEffect(effectiveBookingId) {
        if (!effectiveBookingId.isNullOrEmpty()) {
            val bookingId = effectiveBookingId!!
            // Initial fetch
            val result = bookingRepository.getBooking(bookingId)
            result.onSuccess { booking ->
                currentBookingId = booking.id
                pickupQuery = booking.pickup
                dropOffQuery = booking.dropOff
                selectedSeat = if (booking.seatType.startsWith("4")) "4-Seat" else "6-Seat"
                pickupLatLng = LatLng(booking.pickupLat, booking.pickupLng)
                dropOffLatLng = LatLng(booking.dropOffLat, booking.dropOffLng)
                isSearching = true
            }

            // Real-time status listener
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val docRef = db.collection("bookings").document(bookingId)
            val registration = docRef.addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                
                val status = snapshot.getString("status")

                if (status == "ACCEPTED" || status == "ONGOING") {
                    scope.launch {
                        navController.navigate("cust_journey_details/${bookingId}/${selectedPaymentMethod?.name ?: "CASH"}") {
                            popUpTo(NavRoutes.CustomerDashboard.route) { inclusive = true }
                        }
                    }
                }
            }

            // Real-time offers listener
            val offersRef = docRef.collection("offers")
            val offersRegistration = offersRef.addSnapshotListener { offersSnapshot, offersError ->
                if (offersError != null || offersSnapshot == null) return@addSnapshotListener
                
                val offers = offersSnapshot.documents.mapNotNull { doc ->
                    val driverId = doc.getString("driverId") ?: ""
                    val driverName = doc.getString("driverName") ?: ""
                    val fare = doc.getString("fare") ?: ""
                    val vehicleType = doc.getString("vehicleType") ?: ""
                    val vehiclePlateNumber = doc.getString("vehiclePlateNumber") ?: ""
                    val phoneNumber = doc.getString("phoneNumber") ?: ""

                    DriverOffer(
                        name = driverName,
                        fareLabel = "RM $fare",
                        carBrand = vehicleType,
                        carName = "",
                        carColor = "",
                        plate = vehiclePlateNumber,
                        driverId = driverId,
                        driverPhone = phoneNumber
                    )
                }
                rideOffers = offers
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



                    // Payment Method Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Payment Method",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PaymentMethodCard(
                                title = "DuitNow",
                                icon = Icons.Default.QrCode,
                                isSelected = selectedPaymentMethod == PaymentMethod.QR_DUITNOW,
                                onClick = { selectedPaymentMethod = PaymentMethod.QR_DUITNOW },
                                modifier = Modifier.weight(1f)
                            )
                            PaymentMethodCard(
                                title = "Cash",
                                icon = Icons.Default.AttachMoney,
                                isSelected = selectedPaymentMethod == PaymentMethod.CASH,
                                onClick = { selectedPaymentMethod = PaymentMethod.CASH },
                                modifier = Modifier.weight(1f)
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

                    if (rideOffers.isNotEmpty()) {
                        Text(
                            text = "Driver Offers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        rideOffers.forEach { offer ->
                            OfferCard(
                                offer = offer,
                                onAccept = {
                                    scope.launch {
                                        try {
                                            val bookingId = effectiveBookingId?.takeIf { it.isNotEmpty() } ?: return@launch
                                        
                                        // Update booking with the accepted offer details
                                        val acceptedOffer = com.example.goukm.ui.booking.Offer(
                                            driverId = offer.driverId,
                                            driverName = offer.name,
                                            vehicleType = offer.carBrand,
                                            vehiclePlateNumber = offer.plate,
                                            phoneNumber = offer.driverPhone,
                                            fare = offer.fareLabel.replace("RM ", "")
                                        )
                                        
                                        bookingRepository.acceptOffer(bookingId, acceptedOffer)
                                        
                                        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                                        if (currentUser != null) {
                                            val customerProfile = com.example.goukm.ui.userprofile.UserProfileRepository.getUserProfile(currentUser.uid)
                                            com.example.goukm.ui.chat.ChatRepository.createChatRoom(
                                                bookingId = bookingId,
                                                customerId = currentUser.uid,
                                                driverId = offer.driverId,
                                                customerName = customerProfile?.name ?: "Customer",
                                                driverName = offer.name,
                                                customerPhone = customerProfile?.phoneNumber ?: "",
                                                driverPhone = offer.driverPhone
                                            )
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            android.widget.Toast.makeText(context, "Acceptance error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            )
                            Spacer(Modifier.height(8.dp))
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
                                            dropOffLng = dropOffLng,
                                            paymentMethod = selectedPaymentMethod?.name ?: "CASH"
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
                    shape = RoundedCornerShape(12.dp),
                    enabled = isSearching || selectedPaymentMethod != null // Disable if no payment method selected when not searching
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


@Composable
fun PaymentMethodCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) CBlue else Color.Transparent
    val backgroundColor = if (isSelected) CBlue.copy(alpha = 0.1f) else Color(0xFFF2F3F5)

    Card(
        modifier = modifier
            .height(80.dp)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) CBlue else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) CBlue else Color.Gray
                )
            }
            
            RadioButton(
                selected = isSelected,
                onClick = null, // Handled by Card click
                colors = RadioButtonDefaults.colors(
                    selectedColor = CBlue,
                    unselectedColor = Color.Gray
                )
            )
        }
    }
}



@Composable
fun OfferCard(
    offer: DriverOffer,
    onAccept: () -> Unit
) {
    val yellow = Color(0xFFFFD60A)
    val cardBg = Color.White
    val grayBg = Color(0xFFF6F6F6)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.width(72.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(grayBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }
                Text(offer.name.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Spacer(Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("Car Brand: ${offer.carBrand}", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                if (offer.carName.isNotEmpty()) Text("Car Name: ${offer.carName}", fontSize = 12.sp, color = Color.Black)
                if (offer.carColor.isNotEmpty()) Text("Car Color: ${offer.carColor}", fontSize = 12.sp, color = Color.Black)
                Text("Number Plate: ${offer.plate}", fontSize = 12.sp, color = Color.Black)
            }

            Spacer(Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(grayBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(offer.fareLabel, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = yellow),
                    modifier = Modifier.wrapContentWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Accept", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

