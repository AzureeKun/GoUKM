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
import androidx.compose.runtime.*
import androidx.compose.material.icons.filled.*
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
import com.example.goukm.ui.theme.CBlue

// -----------------------------
// Screen
// -----------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverRideBookingHistoryScreen(
    navController: androidx.navigation.NavController? = null,
    viewModel: DriverHistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedStatus by remember { mutableStateOf("All") }
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF8F9FA), Color.White)
                    )
                )
                .padding(20.dp)
        ) {
            // Back Button
            IconButton(
                onClick = { navController?.popBackStack() },
                modifier = Modifier.offset(x = (-12).dp) // Align flush with padding
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1A1A1A)
                )
            }

            // Header
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "Ride History",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp
                    ),
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your completed rides and earnings",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF6B7280)
                )
            }

            // Filter Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Tabs (Simplified)
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier.height(44.dp).weight(1f)
                ) {
                    Row {
                        listOf("All", "Completed", "Cancelled").forEach { status ->
                            val isSelected = selectedStatus == status
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { selectedStatus = status },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = status,
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
                                            .fillMaxWidth(0.6f)
                                            .background(Color(0xFF3B82F6), RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Ride List
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (val state = uiState) {
                    is DriverHistoryUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is DriverHistoryUiState.Error -> {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is DriverHistoryUiState.Success -> {
                        val filteredRides = state.rides.filter {
                            when (selectedStatus) {
                                "All" -> true
                                "Completed" -> it.status == "COMPLETED"
                                "Cancelled" -> it.status.contains("CANCELLED")
                                else -> true
                            }
                        }

                        if (filteredRides.isEmpty()) {
                            EmptyHistoryState(selectedStatus)
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredRides) { ride ->
                                    DriverRideListItem(
                                        ride = ride,
                                        onClick = {
                                            navController?.navigate("driver_ride_details/${ride.id}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(status: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFD1D5DB)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No $status rides found",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF9CA3AF)
        )
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
            .height(36.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = Color(0xFF374151)
            )
        }
    }
}

@Composable
private fun DriverRideListItem(ride: DriverRide, onClick: () -> Unit) {
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
                Text(
                    text = "Ride for ${ride.customer}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "To ${ride.destination}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )
            }

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = ride.dateTime,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (ride.fare.startsWith("RM")) ride.fare else "RM ${ride.fare}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = Color(0xFF059669))
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = when {
                    ride.status == "COMPLETED" -> Color(0xFFDCFCE7)
                    ride.status.contains("CANCELLED") -> Color(0xFFFEE2E2)
                    else -> Color(0xFFFFF7ED)
                },
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = when {
                        ride.status == "COMPLETED" -> "Done"
                        ride.status.contains("CANCELLED") -> "Cancelled"
                        else -> ride.status.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = when {
                        ride.status == "COMPLETED" -> Color(0xFF166534)
                        ride.status.contains("CANCELLED") -> Color(0xFF991B1B)
                        else -> Color(0xFF92400E)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

// -----------------------------
// Data
// -----------------------------
data class DriverRide(
    val id: String,
    val customer: String,
    val destination: String,
    val dateTime: String,
    val distance: String,
    val fare: String,
    val status: String
)
