package com.example.goukm.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goukm.ui.booking.Booking
import com.example.goukm.ui.booking.BookingRepository
import com.example.goukm.ui.booking.BookingStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.navigation.NavController
import com.example.goukm.navigation.NavRoutes

// --- DATA MODELS ---
enum class RideStatus { COMPLETED, CANCELLED, IN_PROGRESS }

data class RideHistory(
    val id: String,
    val destination: String,
    val pickupPoint: String,
    val dateTime: String, // "7 May 2025, 1.34 PM"
    val price: String,
    val status: RideStatus
)

// Helper function to convert Booking to RideHistory
private fun Booking.toRideHistory(): RideHistory {
    val formatter = SimpleDateFormat("d MMM yyyy, h.mm a", Locale.ENGLISH)
    val dateTimeString = formatter.format(timestamp)
    
    // Map BookingStatus to RideStatus
    val rideStatus = when (status) {
        BookingStatus.COMPLETED.name -> RideStatus.COMPLETED
        BookingStatus.CANCELLED.name,
        BookingStatus.CANCELLED_BY_DRIVER.name,
        BookingStatus.CANCELLED_BY_CUSTOMER.name -> RideStatus.CANCELLED
        BookingStatus.ONGOING.name,
        BookingStatus.ACCEPTED.name -> RideStatus.IN_PROGRESS
        else -> RideStatus.COMPLETED // Default for other statuses
    }
    
    // Format price - use offeredFare if available, otherwise default
    val priceString = if (offeredFare.isNotEmpty()) {
        if (offeredFare.startsWith("RM")) offeredFare else "RM $offeredFare"
    } else {
        "RM 0.00"
    }
    
    return RideHistory(
        id = id,
        destination = dropOff,
        pickupPoint = pickup,
        dateTime = dateTimeString,
        price = priceString,
        status = rideStatus
    )
}


// --- UI ---
@Composable
fun CustomerBookingHistoryScreen(navController: NavController? = null) {
    var selectedStatus by remember { mutableStateOf("All") }
    var selectedPeriod by remember { mutableStateOf("All") }
    var showPeriodFilter by remember { mutableStateOf(false) }
    
    // State for bookings
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val bookingRepository = remember { BookingRepository() }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val scope = rememberCoroutineScope()
    
    // Date formatter for parsing ride.dateTime
    val formatter = SimpleDateFormat("d MMM yyyy, h.mm a", Locale.ENGLISH)
    val now = Date()
    
    // Fetch bookings from Firestore
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            errorMessage = "User not logged in"
            isLoading = false
            return@LaunchedEffect
        }
        
        isLoading = true
        errorMessage = null
        
        val result = bookingRepository.getAllCustomerBookings(currentUser.uid)
        result.onSuccess { bookingList ->
            bookings = bookingList
            isLoading = false
        }.onFailure { exception ->
            errorMessage = "Failed to load bookings: ${exception.message}"
            isLoading = false
        }
    }
    
    // Filtered bookings based on status + period
    val filteredRides = remember(bookings, selectedStatus, selectedPeriod) {
        bookings.filter { booking ->
            // Map BookingStatus to RideStatus for selection
            val rideStatus = when (booking.status) {
                BookingStatus.COMPLETED.name -> RideStatus.COMPLETED
                BookingStatus.CANCELLED.name,
                BookingStatus.CANCELLED_BY_DRIVER.name,
                BookingStatus.CANCELLED_BY_CUSTOMER.name -> RideStatus.CANCELLED
                BookingStatus.ONGOING.name,
                BookingStatus.ACCEPTED.name -> RideStatus.IN_PROGRESS
                else -> RideStatus.COMPLETED
            }

            // Status filter
            val statusMatches = when (selectedStatus) {
                "All" -> true
                "Completed" -> rideStatus == RideStatus.COMPLETED
                "In Progress" -> rideStatus == RideStatus.IN_PROGRESS
                "Cancelled" -> rideStatus == RideStatus.CANCELLED
                else -> true
            }

            // Period filter
            val daysSinceRide = TimeUnit.MILLISECONDS.toDays(now.time - booking.timestamp.time)
            val periodMatches = when (selectedPeriod) {
                "All" -> true
                "1 Month" -> daysSinceRide <= 30
                "3 Months" -> daysSinceRide <= 90
                "6 Months" -> daysSinceRide <= 180
                else -> true
            }

            statusMatches && periodMatches
        }.map { it.toRideHistory() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF8F9FA), Color.White)
                )
            )
            .padding(20.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = "Booking History",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                ),
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your ride history at a glance",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF6B7280)
            )
        }

        // Filter Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Filter (Segmented)
            FilterSegmentControl(
                items = listOf("All", "Completed", "Cancelled", "In Progress"),
                selectedItem = selectedStatus,
                onItemSelected = { selectedStatus = it }
            )

            // Period Filter (Dropdown Icon - Aligned to Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box {
                    FilterIconButton(
                        icon = Icons.Default.DateRange,
                        label = selectedPeriod,
                        onClick = { showPeriodFilter = true }
                    )

                    DropdownMenu(
                        expanded = showPeriodFilter,
                        onDismissRequest = { showPeriodFilter = false }
                    ) {
                        listOf("All", "1 Month", "3 Months", "6 Months").forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period) },
                                onClick = {
                                    selectedPeriod = period
                                    showPeriodFilter = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Error message display
        errorMessage?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF991B1B),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Ride list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (filteredRides.isEmpty()) {
                    item { 
                        EmptyHistoryState(
                            selectedStatus = selectedStatus,
                            onBookRideClick = { 
                                navController?.navigate(NavRoutes.CustomerDashboard.route) {
                                    popUpTo(NavRoutes.CustomerDashboard.route) { inclusive = true }
                                }
                            }
                        ) 
                    }
                } else {
                    items(filteredRides) { ride -> 
                        BookingHistoryListItem(
                            ride = ride,
                            onClick = {
                                navController?.navigate("ride_details/${ride.id}")
                            }
                        ) 
                    }
                }
            }
        }
    }
}

// --- Components ---
@Composable
private fun FilterSegmentControl(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(44.dp),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Row {
            items.forEach { item ->
                val isSelected = selectedItem == item
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onItemSelected(item) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = if (isSelected) Color(0xFF3B82F6) else Color(0xFF6B7280)
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .height(3.dp)
                                .fillMaxWidth(0.9f)
                                .background(Color(0xFF3B82F6), RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterIconButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF374151), fontWeight = FontWeight.SemiBold)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun BookingHistoryListItem(ride: RideHistory, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Brush.horizontalGradient(colors = listOf(Color(0xFFF3F4F6), Color(0xFFE5E7EB), Color(0xFFF3F4F6))))
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Ride to ${ride.destination}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF111827)))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Pickup point ${ride.pickupPoint}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280))
            }

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 16.dp)) {
                Text(ride.dateTime, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = Color(0xFF6B7280), textAlign = TextAlign.End)
                Spacer(modifier = Modifier.height(8.dp))
                Text(ride.price, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = Color(0xFF059669)))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = when (ride.status) {
                    RideStatus.COMPLETED -> Color(0xFFDCFCE7)
                    RideStatus.IN_PROGRESS -> Color(0xFFFFF7ED)
                    RideStatus.CANCELLED -> Color(0xFFFEE2E2)
                },
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = when (ride.status) {
                        RideStatus.COMPLETED -> "Done"
                        RideStatus.IN_PROGRESS -> "In Progress"
                        RideStatus.CANCELLED -> "Cancel"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = when (ride.status) {
                        RideStatus.COMPLETED -> Color(0xFF166534)
                        RideStatus.IN_PROGRESS -> Color(0xFF92400E)
                        RideStatus.CANCELLED -> Color(0xFF991B1B)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun EmptyHistoryState(selectedStatus: String, onBookRideClick: () -> Unit) {
    // Define content based on the active tab
    val (title, description, icon) = when (selectedStatus) {
        "In Progress" -> Triple(
            "No active rides",
            "You don't have any rides in progress. Ready to start a new journey?",
            Icons.Default.DirectionsCar
        )
        "Completed" -> Triple(
            "No completed rides",
            "Your completed trips will appear here. Take your first ride today!",
            Icons.Default.History
        )
        "Cancelled" -> Triple(
            "No cancelled rides",
            "All your rides have been smooth so far! No cancellations found.",
            Icons.Default.Cancel
        )
        else -> Triple(
            "No records found",
            "Book your first ride to see your journey history here.",
            Icons.Default.HistoryToggleOff
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp, horizontal = 24.dp)
    ) {
        // Modern, subtle icon container
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFFF3F4F6), RoundedCornerShape(60.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Primary Action Button
        Button(
            onClick = onBookRideClick,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                "Book a Ride",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHistory() {
    MaterialTheme { CustomerBookingHistoryScreen() }
}
