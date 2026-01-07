package com.example.goukm.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// -----------------------------
// Screen
// -----------------------------
// -----------------------------
// Screen
// -----------------------------
@Composable
fun DriverRideBookingHistoryScreen(
    viewModel: DriverHistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
        // Header Row: Title + Filter Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Ride History",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    ),
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your completed rides and earnings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )
            }

            // Filter Icon Button on the same row
            FilterIconButton(Icons.Default.FilterList, "All") { /* Filter action */ }
        }

        Spacer(modifier = Modifier.height(4.dp))


        // Ride List
        when (val state = uiState) {
            is DriverHistoryUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is DriverHistoryUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = Color.Red)
                }
            }
            is DriverHistoryUiState.Success -> {
                if (state.rides.isEmpty()) {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No ride history yet", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.rides) { ride ->
                            Column {
                                DriverRideListItem(ride = ride)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFFF3F4F6),
                                                    Color(0xFFE5E7EB),
                                                    Color(0xFFF3F4F6)
                                                )
                                            )
                                        )
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
private fun FilterIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .height(36.dp), // smaller height
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(10.dp) // slightly smaller rounding
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp), // less padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp) // tighter spacing
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(16.dp) // smaller icon
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp // smaller font
                ),
                color = Color(0xFF374151)
            )
        }
    }
}


@Composable
private fun DriverRideListItem(ride: DriverRide) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
            shape = CircleShape,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Default.Person, contentDescription = "Customer", tint = Color(0xFF9CA3AF))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Ride Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ride.customer,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                ),
                maxLines = 1
            )
            Text(
                text = "To ${ride.destination}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = Color(0xFF6B7280),
                maxLines = 1
            )
             // Status Indicator
            val statusColor = when (ride.status) {
                "COMPLETED" -> Color(0xFF059669) // Green
                "CANCELLED", "CANCELLED_BY_DRIVER", "CANCELLED_BY_CUSTOMER" -> Color(0xFFEF4444) // Red
                else -> Color.Gray
            }
             Text(
                text = ride.status.replace("_", " "),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = statusColor,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Date + Distance + Fare
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = ride.dateTime,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = ride.distance,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF3B82F6) // blue accent
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = ride.fare,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF059669),
                    fontSize = 16.sp
                )
            )
        }
    }
}

// -----------------------------
// Data
// -----------------------------
data class DriverRide(
    val customer: String,
    val destination: String,
    val dateTime: String,
    val distance: String,
    val fare: String,
    val status: String
)
