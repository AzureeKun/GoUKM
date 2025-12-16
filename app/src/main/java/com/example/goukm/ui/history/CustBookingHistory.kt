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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// --- DATA MODELS ---
enum class RideStatus { COMPLETED, CANCELLED, IN_PROGRESS }

data class RideHistory(
    val destination: String,
    val pickupPoint: String,
    val dateTime: String, // "7 May 2025, 1.34 PM"
    val price: String,
    val status: RideStatus
)

// Sample ride history
val rideHistoryData = listOf(
    RideHistory("Kolej Aminuddin Baki", "Kolej Keris", "7 May 2025, 1.34 PM", "RM 6.00", RideStatus.COMPLETED),
    RideHistory("Kolej Ibrahim Yaakub", "Kolej Pendeta Zaba", "8 May 2025, 10.34 PM", "RM 5.00", RideStatus.IN_PROGRESS),
    RideHistory("Kolej Aminuddin Baki", "Kolej Keris", "2 April 2025, 2.30 PM", "RM 6.00", RideStatus.COMPLETED),
    RideHistory("Kolej Ibrahim Yaakub", "Kolej Pendeta Zaba", "1 May 2025, 10.00 PM", "RM 5.00", RideStatus.CANCELLED)
)

// --- UI ---
@Composable
fun CustomerBookingHistoryScreen() {
    var selectedStatus by remember { mutableStateOf("All") }
    var selectedPeriod by remember { mutableStateOf("All") }
    var showPeriodFilter by remember { mutableStateOf(false) }

    // Date formatter for parsing ride.dateTime
    val formatter = SimpleDateFormat("d MMM yyyy, h.mm a", Locale.ENGLISH)
    val now = Date()

    // Filtered rides based on status + period
    val filteredRides = rideHistoryData.filter { ride ->
        // Status filter
        val statusMatches = when (selectedStatus) {
            "All" -> true
            "Completed" -> ride.status == RideStatus.COMPLETED
            "In Progress" -> ride.status == RideStatus.IN_PROGRESS
            "Cancelled" -> ride.status == RideStatus.CANCELLED
            else -> true
        }

        // Period filter
        val periodMatches = try {
            val rideDate = formatter.parse(ride.dateTime)
            when (selectedPeriod) {
                "All" -> true
                "1 Month" -> TimeUnit.MILLISECONDS.toDays(now.time - rideDate.time) <= 30
                "3 Months" -> TimeUnit.MILLISECONDS.toDays(now.time - rideDate.time) <= 90
                "6 Months" -> TimeUnit.MILLISECONDS.toDays(now.time - rideDate.time) <= 180
                else -> true
            }
        } catch (e: Exception) {
            true
        }

        statusMatches && periodMatches
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

            // Period Filter (Dropdown Icon)
            Row(horizontalArrangement = Arrangement.End) {
                Box {
                    FilterIconButton(
                        icon = Icons.Default.DateRange,
                        label = selectedPeriod,
                        onClick = { showPeriodFilter = true }
                    )

                    DropdownMenu(
                        expanded = showPeriodFilter,
                        onDismissRequest = { showPeriodFilter = false },
                        modifier = Modifier.align(Alignment.TopStart)
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

        // Ride list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (filteredRides.isEmpty()) {
                item { EmptyHistoryState() }
            } else {
                items(filteredRides) { ride -> BookingHistoryListItem(ride) }
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
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF374151), fontWeight = FontWeight.Medium)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun BookingHistoryListItem(ride: RideHistory) {
    Column {
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

            IconButton(
                onClick = { /* Ride menu */ },
                modifier = Modifier.size(36.dp).background(Color(0xFFF9FAFB), RoundedCornerShape(20.dp))
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "More ride options", tint = Color(0xFF6B7280), modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun EmptyHistoryState() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 80.dp)) {
        Icon(Icons.Default.HistoryToggleOff, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("You have no booking history yet.", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium, color = Color(0xFF374151)), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Book your first ride to see your journey here", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { /* Navigate to booking */ }, modifier = Modifier.fillMaxWidth(0.6f).height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))) {
            Text("Book a Ride", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHistory() {
    MaterialTheme { CustomerBookingHistoryScreen() }
}
