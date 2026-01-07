package com.example.goukm.ui.driver

import com.example.goukm.ui.theme.CBlue

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.goukm.navigation.NavRoutes
import com.example.goukm.ui.dashboard.BottomNavigationBarDriver
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.goukm.ui.booking.Booking
import com.example.goukm.ui.booking.Journey
import java.time.ZoneId
import java.util.Date

val LightGreen = Color(0xFF4CAF50)

// Data Models
data class BarData(
    val label: String,
    val value: Float,
    val fullDate: String = "" 
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverEarningScreen(
    navController: NavHostController,
    viewModel: DriverEarningViewModel = viewModel()
) {
    var selectedNavIndex = 2
    
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val graphGranularity by viewModel.graphGranularity.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val newDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        // Since we don't have a direct setDate, we'll need to move it or add a setDate method.
                        // For simplicity, let's assume we can add setDate to ViewModel or just move it relative.
                        // I'll add setDate to ViewModel.
                        viewModel.moveDateByEpoch(it)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val dateRangeText = remember(selectedPeriod, currentDate) {
        getDateRangeLabel(selectedPeriod, currentDate)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Earnings",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CBlue)
            )
        },
        bottomBar = {
            BottomNavigationBarDriver(
                selectedIndex = selectedNavIndex,
                onSelected = { index ->
                    selectedNavIndex = index
                    when (index) {
                        0 -> navController.navigate(NavRoutes.DriverDashboard.route) {
                            popUpTo(NavRoutes.DriverDashboard.route) { inclusive = true }
                        }
                        1 -> navController.navigate(NavRoutes.DriverScore.route)
                        2 -> { /* Already here */ }
                        3 -> navController.navigate(NavRoutes.DriverProfile.route)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            
            // 1. Main Period Selector (Day, Week, Month, Year)
            item {
                PeriodSelector(
                    periods = listOf("Day", "Week", "Month", "Year"),
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { viewModel.setPeriod(it) }
                )
            }

            // 2. Earnings Summary & Navigation Card
            item {
                val formattedOnlineTime = remember(uiState.totalOnlineMinutes) {
                    val hours = uiState.totalOnlineMinutes / 60
                    val minutes = uiState.totalOnlineMinutes % 60
                    "${hours}h ${minutes}m"
                }

                EarningsNavigationCard(
                    dateRangeText = dateRangeText,
                    earnings = uiState.totalEarnings,
                    rides = uiState.rideCount,
                    hours = formattedOnlineTime,
                    description = "this ${selectedPeriod.lowercase()}",
                    onPrevClick = { viewModel.moveDate(-1) },
                    onNextClick = { viewModel.moveDate(1) },
                    onDateClick = { showDatePicker = true }
                )
            }

            // 3. Graph Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = dateRangeText, 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { showDatePicker = true }
                        )
                        Spacer(Modifier.height(8.dp))

                        // Sub-selector (if applicable)
                        if (selectedPeriod == "Month" || selectedPeriod == "Year") {
                           SubPeriodSelector(
                               mainPeriod = selectedPeriod,
                               selectedSubPeriod = graphGranularity,
                               onSelect = { viewModel.setGranularity(it) }
                           )
                           Spacer(Modifier.height(12.dp))
                        }
                        
                        Text(
                            "Number of rides",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Bar Chart
                        BarChart(
                            data = uiState.graphData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }

            // 4. Recent Ride Header & Transaction
            if (uiState.recentRide != null) {
                item {
                    Text(
                        "Recent Ride",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                item {
                     RecentRideCard(uiState.recentRide!!)
                }
            } else {
                item {
                    EmptyStateCard(message = "No rides found for this period")
                }
            }
            
            item {
                Button(
                    onClick = { navController.navigate(NavRoutes.DriverRideHistory.route) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("View Ride History", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// --- Helper Components ---

@Composable
fun PeriodSelector(
    periods: List<String>,
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(50.dp), // Pill shape container
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(4.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            periods.forEach { period ->
                val isSelected = selectedPeriod == period
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) CBlue else Color.Transparent)
                        .clickable { onPeriodSelected(period) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = period,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SubPeriodSelector(
    mainPeriod: String,
    selectedSubPeriod: String,
    onSelect: (String) -> Unit
) {
    val options = when(mainPeriod) {
        "Month" -> listOf("Week", "Day") // Week first
        "Year" -> listOf("Month", "Week", "Day")
        else -> emptyList()
    }
    
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = selectedSubPeriod == option
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(option) },
                label = { Text(option) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CBlue.copy(alpha = 0.1f),
                    selectedLabelColor = CBlue
                )
            )
        }
    }
}

@Composable
fun EarningsNavigationCard(
    dateRangeText: String,
    earnings: Double,
    rides: Int,
    hours: String,
    description: String,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onDateClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onDateClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                description,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(8.dp))
            
            // Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevClick) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Previous", tint = Color.Gray)
                }
                
                Text(
                    text = "RM ${String.format("%.2f", earnings)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                IconButton(onClick = onNextClick) {
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next", tint = Color.Gray)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                     Icon(Icons.Default.TrendingUp, contentDescription = null, tint = CBlue)
                     Spacer(Modifier.width(8.dp))
                     Text("$rides Rides", fontWeight = FontWeight.SemiBold, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                     Icon(
                         Icons.Default.AttachMoney, 
                         contentDescription = null, tint = Color.Black 
                     )
                     Spacer(Modifier.width(8.dp))
                     Text(hours, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.TrendingUp, 
                contentDescription = null, 
                tint = Color.LightGray, 
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(message, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun BarChart(
    data: List<BarData>,
    modifier: Modifier = Modifier
) {
    // If all values are 0, it's an empty state
    val maxVal = if (data.isEmpty()) 0f else data.maxOf { it.value }
    val isEmpty = maxVal == 0f
    
    // Dynamically set yStep based on maxVal for better granularity
    val yStep = if (maxVal <= 10f) 1 else 5
    val yMax = if (maxVal == 0f) 5f else (kotlin.math.ceil(maxVal / yStep) * yStep).toFloat()
    
    // Y-Axis labels
    val yLabels = (0..yMax.toInt() step yStep).toList().reversed()

    Row(modifier = modifier) {
        // Y-Axis labels column remains the same...
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            yLabels.forEach { label ->
               Text(
                   text = label.toString(),
                   fontSize = 10.sp,
                   color = Color.Gray,
                   modifier = Modifier.padding(end = 4.dp)
               )
            }
            Spacer(Modifier.height(20.dp)) 
        }

        // Chart Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Grid Lines and Bars logic...
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                 yLabels.forEach { _ ->
                     HorizontalDivider(
                         color = Color.LightGray.copy(alpha = 0.5f),
                         thickness = 1.dp,
                         modifier = Modifier.padding(bottom = (if (yLabels.indexOf(0) == yLabels.size -1) 20.dp else 0.dp))
                     )
                 }
                 Spacer(Modifier.height(20.dp))
            }

            // Bars
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        val barFraction = (item.value / yMax).coerceIn(0f, 1f)
                        
                        if (barFraction < 1f) {
                            Spacer(Modifier.weight(1f - barFraction))
                        }
                        
                        if (barFraction > 0f) {
                            // Peak hour highlight logic
                            val isPeak = item.value == maxVal && maxVal > 0
                            val barColor = if (isPeak) Color(0xFFFFA000) else Color(0xFF4285F4)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .weight(barFraction)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(barColor)
                            )
                        } else {
                            Spacer(Modifier.weight(0.0001f))
                        }
                    }
                }
            }
            
            // X-Axis Labels
            Row(
                 modifier = Modifier
                    .fillMaxSize()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                 data.forEachIndexed { index, item ->
                     // If too many items (like hours), show every 3rd label to avoid crowding
                     val showLabel = data.size <= 12 || (index % 3 == 0)
                     
                     Text(
                        text = if (showLabel) item.label.replace("AM", "A").replace("PM", "P") else "",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 4.dp)
                    )
                 }
            }
        }
    }
}

@Composable
fun RecentRideCard(journey: Journey) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - h:mm a")
    val dateStr = journey.timestamp.toInstant().atZone(ZoneId.systemDefault()).format(formatter)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CUSTOMER #${journey.userId.takeLast(4).uppercase()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = dateStr,
                            color = Color(0xFFFFA000),
                            fontSize = 12.sp
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Trip Total", fontSize = 10.sp, color = Color.Gray)
                    Text("RM ${journey.offeredFare}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // Pickup & Drop-off
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = null,
                    tint = Color(0xFF4285F4),
                    modifier = Modifier.size(8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = journey.pickup,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Box(
                modifier = Modifier
                    .padding(start = 3.5.dp)
                    .width(1.dp)
                    .height(12.dp)
                    .background(Color.LightGray)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFEA4335),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = journey.dropOff,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}



fun getDateRangeLabel(period: String, date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
    return when (period) {
        "Day" -> "Today, ${date.format(DateTimeFormatter.ofPattern("d MMMM"))}"
        "Week" -> {
            val startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            "${startOfWeek.format(DateTimeFormatter.ofPattern("d MMM"))} - ${endOfWeek.format(DateTimeFormatter.ofPattern("d MMM"))}"
        }
        "Month" -> date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        "Year" -> date.format(DateTimeFormatter.ofPattern("yyyy"))
        else -> ""
    }
}
