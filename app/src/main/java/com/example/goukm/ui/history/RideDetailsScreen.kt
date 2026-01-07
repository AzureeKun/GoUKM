package com.example.goukm.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.goukm.ui.booking.Booking
import com.example.goukm.ui.booking.BookingRepository
import com.example.goukm.ui.booking.BookingStatus
import com.example.goukm.ui.booking.Journey
import com.example.goukm.ui.booking.JourneyRepository
import com.example.goukm.ui.userprofile.UserProfile
import com.example.goukm.ui.userprofile.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideDetailsScreen(
    navController: NavController,
    bookingId: String
) {
    val scope = rememberCoroutineScope()
    val bookingRepository = remember { BookingRepository() }
    val journeyRepository = JourneyRepository
    val userRepository = UserProfileRepository
    
    var booking by remember { mutableStateOf<Booking?>(null) }
    var journey by remember { mutableStateOf<Journey?>(null) }
    var driver by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val dateFormatter = SimpleDateFormat("EEEE, d MMM yyyy", Locale.ENGLISH)
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.ENGLISH)

    LaunchedEffect(bookingId) {
        isLoading = true
        val bookingResult = bookingRepository.getBooking(bookingId)
        bookingResult.onSuccess { b ->
            booking = b
            // If completed, fetch journey details
            if (b.status == BookingStatus.COMPLETED.name) {
                val journeyResult = journeyRepository.getJourney(bookingId)
                journeyResult.onSuccess { j: Journey -> journey = j }
            }
            
            // Fetch driver details if there's a driverId
            if (b.driverId.isNotEmpty()) {
                val driverProfile = userRepository.getUserProfile(b.driverId)
                if (driverProfile != null) {
                    driver = driverProfile
                }
            }
            isLoading = false
        }.onFailure { e ->
            error = "Failed to load booking: ${e.message}"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ride Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = error ?: "Unknown error", textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                }
            } else if (booking != null) {
                val b = booking!!
                val isCancelled = b.status.contains("CANCELLED")
                val isCompleted = b.status == BookingStatus.COMPLETED.name

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header Section
                    Text(
                        text = dateFormatter.format(b.timestamp),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = timeFormatter.format(b.timestamp),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        ),
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Booking ID: #${b.id.take(8).uppercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9CA3AF)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Driver / Status Section
                    if (isCompleted && driver != null) {
                        DriverCard(driver!!)
                    } else if (isCancelled) {
                        CancellationBanner()
                    } else {
                        // For other statuses (e.g. In Progress)
                        StatusCard(b.status)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Payment Card
                    PaymentCard(
                        method = b.paymentMethod,
                        amount = if (b.offeredFare.startsWith("RM")) b.offeredFare else "RM ${b.offeredFare}"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Route Card
                    RouteTimelineCard(
                        pickup = b.pickup,
                        dropoff = b.dropOff,
                        startTime = timeFormatter.format(b.timestamp),
                        endTime = journey?.timestamp?.let { timeFormatter.format(it) },
                        duration = if (journey != null) {
                            val diff = journey!!.timestamp.time - b.timestamp.time
                            val minutes = (diff / (1000 * 60)).toInt()
                            "$minutes minutes"
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
fun DriverCard(driver: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = Color(0xFFE5E7EB)
            ) {
                Icon(
                    Icons.Default.Person, 
                    contentDescription = null, 
                    modifier = Modifier.padding(12.dp),
                    tint = Color(0xFF9CA3AF)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = driver.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF111827)
                )
                Text(
                    text = "Driver • ${driver.carBrand} ${driver.carColor}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = driver.vehiclePlateNumber,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF3B82F6)
                )
            }
        }
    }
}

@Composable
fun CancellationBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFB91C1C))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Booking Cancelled",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFB91C1C)
            )
        }
    }
}

@Composable
fun StatusCard(status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF0369A1))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Status: $status",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0369A1)
            )
        }
    }
}

@Composable
fun PaymentCard(method: String, amount: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountBalanceWallet, 
                        contentDescription = null, 
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }
                Text(
                    text = if (method == "QR_DUITNOW") "QR DuitNow" else "Cash",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF111827)
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF3F4F6))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Payments, 
                        contentDescription = null, 
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Total Fare",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }
                Text(
                    text = amount,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF059669)
                    )
                )
            }
        }
    }
}

@Composable
fun RouteTimelineCard(
    pickup: String,
    dropoff: String,
    startTime: String,
    endTime: String?,
    duration: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Pickup
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFF3B82F6), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(40.dp)
                            .background(Color(0xFFE5E7EB))
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pickup,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Pickup • $startTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
            }
            
            // Dropoff
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFFEF4444), RoundedCornerShape(2.dp))
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dropoff,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (endTime != null) "Dropoff • $endTime" else "Dropoff",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
            }
            
            if (duration != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    color = Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "duration: $duration",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF4B5563)
                    )
                }
            }
        }
    }
}
