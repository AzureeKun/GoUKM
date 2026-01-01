package com.example.goukm.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun confirmPay(
    totalAmount: String,
    onBack: () -> Unit = {},
    onProceedPayment: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Booking Details",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 36.sp,
                )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF6B87C0)
                )
            )
        },
        containerColor = Color(0xFFE1F5FE),
        bottomBar = {
            Button(
                onClick = onProceedPayment,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 20.dp,  // Removes shadow/box
                    pressedElevation = 0.dp
                )
            ) {
                Text(
                    "Proceed Payment",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6B87C0),
                            Color(0xFFE3F2FD),
                            Color(0xFFE1F5FE)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Seat Info
                        Text(
                            "Seat: 4 seater",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF1976D2),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Pickup Location
                        LocationItem(
                            "Kolej Aminuddin Baki",
                            Icons.Default.ArrowUpward,
                            true
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Dropoff Location
                        LocationItem(
                            "Kolej Pendeta Za'ba",
                            Icons.Default.ArrowDownward,
                            false
                        )
                        Spacer(modifier = Modifier.height(28.dp))

                        // Driver & Car Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Profile
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color.Black, CircleShape)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "ANGELA",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Text(
                                    "KELLY",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Car Details
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Car Brand: Perodua", fontSize = 14.sp)
                                Text("Car Name: Axia", fontSize = 14.sp)
                                Text("Car Color: Silver", fontSize = 14.sp)
                                Text("Plate No: KFM1044", fontSize = 14.sp)
                            }

                            // Total Price
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFFD700), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                            ) {
                                Text(
                                    totalAmount,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
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
fun LocationItem(
    location: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPickup: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .background(
                    if (isPickup) Color(0xFF4CAF50) else Color(0xFFF44336),
                    CircleShape
                )
                .padding(6.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                location,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Text(
                if (isPickup) "Pickup Location" else "Dropoff Location",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun confirmPayPreview() {
    MaterialTheme {
        confirmPay(
            totalAmount = "RM 5",
            onBack = {},
            onProceedPayment = {}
        )
    }
}
