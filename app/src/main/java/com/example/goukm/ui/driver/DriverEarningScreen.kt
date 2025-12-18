package com.example.goukm.ui.driver

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.goukm.navigation.NavRoutes
import com.example.goukm.ui.dashboard.BottomNavigationBarDriver
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import kotlin.random.Random

val LightGreen = Color(0xFF4CAF50)

// Data Models
data class Transaction(
    val id: String,
    val customerName: String,
    val date: String,
    val amount: Double,
    val route: String
)

data class BarData(
    val label: String,
    val value: Float,
    val fullDate: String = "" // For tooltip or details if needed
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverEarningScreen(navController: NavHostController) {
    var selectedNavIndex by remember { mutableStateOf(2) }
    
    // Main Period Selection: Day, Week, Month, Year
    var selectedPeriod by remember { mutableStateOf("Day") }
    
    // Graph Granularity (Sub-selection): e.g. for Year -> Month, Week, Day
    var graphGranularity by remember { mutableStateOf("Hour") } 
    
    // Current Anchor Date
    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    // Update graph granularity default when main period changes
    LaunchedEffect(selectedPeriod) {
        graphGranularity = when (selectedPeriod) {
            "Day" -> "Hour"
            "Week" -> "Day"
            "Month" -> "Day" // Default to Day, can switch to Week
            "Year" -> "Month" // Default to Month, can switch to Week, Day
            else -> "Day"
        }
    }

    // Mock Data Generation based on state
    val graphData = remember(selectedPeriod, graphGranularity, currentDate) {
        generateMockGraphData(selectedPeriod, graphGranularity, currentDate)
    }

    val totalRides = remember(graphData) {
        graphData.sumOf { it.value.toInt() }
    }

    val currentEarnings = remember(totalRides) {
        // Mock average fare between RM 5.00 and 15.00
        totalRides * (5.0 + Random.nextDouble() * 10.0)
    }
    
    val totalHours = "107h 20m" // Mock

    // Date Range Display String
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
                    onPeriodSelected = { selectedPeriod = it }
                )
            }

            // 2. Earnings Summary & Navigation Card
            item {
                EarningsNavigationCard(
                    dateRangeText = dateRangeText,
                    earnings = currentEarnings,
                    rides = totalRides,
                    hours = totalHours,
                    description = "this ${selectedPeriod.lowercase()}",
                    onPrevClick = {
                        currentDate = moveDate(currentDate, selectedPeriod, -1)
                    },
                    onNextClick = {
                        currentDate = moveDate(currentDate, selectedPeriod, 1)
                    }
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
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))

                        // Sub-selector (if applicable)
                        if (selectedPeriod == "Month" || selectedPeriod == "Year") {
                           SubPeriodSelector(
                               mainPeriod = selectedPeriod,
                               selectedSubPeriod = graphGranularity,
                               onSelect = { graphGranularity = it }
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
                            data = graphData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }

            // 4. Recent Ride Header & Mock Transaction
            item {
                Text(
                    "Recent Ride",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            item {
                 // Reusing TransactionCard logic but simpler for "Recent Ride"
                 RecentRideCard()
            }
            
            item {
                Button(
                    onClick = { /* View history */ },
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
        "Month" -> listOf("Day", "Week")
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
    onNextClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                     Icon(// Use a clock icon if available, or simplified
                         Icons.Default.AttachMoney, // Placeholder if no clock icon in scope
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
fun BarChart(
    data: List<BarData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxVal = data.maxOf { it.value }
    // Round up max value to nearest 5 for nicer Y-axis steps
    val yStep = 5
    val yMax = if (maxVal == 0f) 5f else (kotlin.math.ceil(maxVal / yStep) * yStep).toFloat()
    
    // Y-Axis labels (0, 5, 10, ... yMax)
    val yLabels = (0..yMax.toInt() step yStep).toList().reversed()

    Row(modifier = modifier) {
        // Y-Axis Title (Rotated)
        // Since rotating text in a Row is tricky with layout, we'll place it as a vertical text or just a label on top left.
        // The image shows "Number of rides" rotated 90 deg. 
        // For simplicity and stability, we'll put it outside or use a custom layout. 
        // Let's use a standard Column with a Text for the title above or simply a narrow column for axis.
        
        // Actually, let's implement the layout as:
        // Column {
        //   Text("Number of rides", rotated...)
        //   Row { Y-Axis Labels | Chart }
        // }
        // To rotate text: Modifier.rotate(-90f) but it needs size adjustment.
        // Simplification: Just put "Number of rides" at the top-left of the chart area.
        
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            // We can't easily distribute text exactly matching grid lines with simple SpaceBetween if the height isn't fixed relative to data.
            // Better approach: Canvas drawing for grid lines and labels, OR carefully sized Boxes.
            // Let's stick to a simpler approximation for Y-axis labels.
            
            yLabels.forEach { label ->
               Text(
                   text = label.toString(),
                   fontSize = 10.sp,
                   color = Color.Gray,
                   modifier = Modifier.padding(end = 4.dp)
               )
            }
            // Add a spacer for the X-axis labels height
            Spacer(Modifier.height(20.dp)) 
        }

        // Chart Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Grid Lines
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                 yLabels.forEach { _ ->
                     HorizontalDivider(
                         color = Color.LightGray.copy(alpha = 0.5f),
                         thickness = 1.dp,
                         modifier = Modifier.padding(bottom = (if (yLabels.indexOf(0) == yLabels.size -1) 20.dp else 0.dp)) // Adjust for X-label height? No, SpaceBetween handles it roughly.
                         // Actually, SpaceBetween for N items creates N lines.
                     )
                 }
                 Spacer(Modifier.height(20.dp)) // Reserve space for X-axis labels
            }

            // Bars
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 20.dp), // Space for X-axis labels
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
                        // Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .weight(if (item.value <= 0f) 0.001f else (item.value / yMax))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(Color(0xFF4285F4))
                        )
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
                 data.forEach { item ->
                     Text(
                        text = item.label,
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
fun RecentRideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White) // Placeholder avatar
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("ANGELA KELLY", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("23/08/2025 - 8:17 pm", color = Color(0xFFFFA000), fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Trip Total", fontSize = 10.sp, color = Color.Gray)
                Text("RM 10", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}


// --- Logic Helper Functions ---

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

fun moveDate(current: LocalDate, period: String, direction: Int): LocalDate {
    return when (period) {
        "Day" -> current.plusDays(direction.toLong())
        "Week" -> current.plusWeeks(direction.toLong())
        "Month" -> current.plusMonths(direction.toLong())
        "Year" -> current.plusYears(direction.toLong())
        else -> current
    }
}

fun generateMockGraphData(period: String, granularity: String, date: LocalDate): List<BarData> {
    val random = Random(date.hashCode()) // Consistent random per date
    
    return when (granularity) {
        "Hour" -> {
            // 6AM to 11PM
            val hours = listOf("6AM","7AM","8AM","9AM","10AM","11AM","12PM","1PM","2PM","3PM","4PM","5PM","6PM","7PM","11PM")
            hours.map { BarData(it, random.nextFloat() * 5) } // Max 5 rides per hour
        }
        "Day" -> {
            // Mon - Sun for Week view, or 1-30 for Month view
            if (period == "Week") {
                listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun").map { 
                    BarData(it, random.nextFloat() * 15) // Max 15 rides per day
                }
            } else if (period == "Month") {
                 val daysInMonth = date.lengthOfMonth()
                 (1..daysInMonth).map { 
                     BarData(if(it % 5 == 0 || it == 1) "$it" else "", random.nextFloat() * 12)
                 }
            } else { 
                 (1..12).map { BarData("$it", random.nextFloat() * 20) }
            }
        }
        "Week" -> {
            // 4-5 weeks in a month
             (1..4).map { BarData("W$it", random.nextFloat() * 80) } // Max 80 rides per week
        }
        "Month" -> {
            listOf("J","F","M","A","M","J","J","A","S","O","N","D").map {
                BarData(it, random.nextFloat() * 300) // Max 300 rides per month
            }
        }
        else -> emptyList()
    }
}
