package com.example.goukm.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.goukm.ui.dashboard.BottomNavigationBarDriver

val LightGreen = Color(0xFF4CAF50)

data class Transaction(
    val id: String,
    val customerName: String,
    val date: String,
    val amount: Double,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverEarningScreen(navController: NavHostController) {
    var selectedPeriod by remember { mutableStateOf("Day") }
    var selectedNavIndex by remember { mutableStateOf(2) } // Earning is index 2

    // Dummy data
    val dailyEarnings = 45.50
    val monthlyEarnings = 823.75
    val yearlyEarnings = 9240.00

    val dailyRides = 8
    val monthlyRides = 142
    val yearlyRides = 1650

    val transactions = remember {
        listOf(
            Transaction("1", "Siti Aminah", "Today, 3:45 PM", 6.50, "FTSM → Kolej Pendeta"),
            Transaction("2", "Ahmad Ali", "Today, 2:20 PM", 5.00, "Block E → FST"),
            Transaction("3", "Nur Fatimah", "Today, 11:30 AM", 8.00, "Kolej Tun → FTSM"),
            Transaction("4", "Hassan Ibrahim", "Today, 9:15 AM", 4.50, "FTSM → Block E"),
            Transaction("5", "Zainab Omar", "Today, 8:00 AM", 7.50, "Kolej Aminuddin → Library"),
            Transaction("6", "Ali Rahman", "Yesterday, 5:30 PM", 6.00, "FST → Kolej Za'ba"),
            Transaction("7", "Aminah Hassan", "Yesterday, 4:15 PM", 8.00, "Block E → FTSM")
        )
    }

    val currentEarnings = when (selectedPeriod) {
        "Day" -> dailyEarnings
        "Month" -> monthlyEarnings
        else -> yearlyEarnings
    }

    val currentRides = when (selectedPeriod) {
        "Day" -> dailyRides
        "Month" -> monthlyRides
        else -> yearlyRides
    }

    val averageFare = if (currentRides > 0) currentEarnings / currentRides else 0.0

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
                        0 -> navController.navigate("driver_dashboard") {
                            popUpTo("driver_dashboard") { inclusive = true }
                        }
                        1 -> navController.navigate("driver_chat_list")
                        3 -> navController.navigate("driver_profile")
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
            // Period Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Day", "Month", "Year").forEach { period ->
                            PeriodChip(
                                label = period,
                                isSelected = selectedPeriod == period,
                                onClick = { selectedPeriod = period }
                            )
                        }
                    }
                }
            }

            // Total Earnings Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CBlue),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Total Earnings",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "RM",
                                fontSize = 24.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                String.format("%.2f", currentEarnings),
                                fontSize = 48.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "this $selectedPeriod",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Statistics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Rides
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Rides",
                        value = currentRides.toString(),
                        icon = Icons.Default.TrendingUp,
                        backgroundColor = Color(0xFFE3F2FD),
                        iconColor = Color(0xFF1976D2)
                    )
                    
                    // Average Fare
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Avg Fare",
                        value = "RM ${String.format("%.2f", averageFare)}",
                        icon = Icons.Default.AttachMoney,
                        backgroundColor = Color(0xFFFFF9C4),
                        iconColor = Color(0xFFF57C00)
                    )
                }
            }

            // Transaction History Header
            item {
                Text(
                    "Recent Transactions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Transaction List
            items(transactions) { transaction ->
                TransactionCard(transaction)
            }
        }
    }
}

@Composable
fun PeriodChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) CBlue else Color(0xFFE0E0E0),
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .height(40.dp)
            .widthIn(min = 80.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                title,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(LightGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = LightGreen,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Transaction Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.customerName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    transaction.route,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    transaction.date,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Amount
            Text(
                "+RM ${String.format("%.2f", transaction.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LightGreen
            )
        }
    }
}
