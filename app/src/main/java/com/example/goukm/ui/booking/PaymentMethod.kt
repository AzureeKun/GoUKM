package com.example.goukm.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(
    totalAmount: String = "RM 4",
    onPaymentConfirmed: (String) -> Unit = {}
) {
    var showCashConfirm by remember { mutableStateOf(false) }
    var showQRDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Select Payment Method",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF6B87C0)
                )
            )
        },
        containerColor = Color.Transparent
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
                // DuitNow QR Card
                PaymentOptionCard(
                    iconBgColor = Color(0xFFE91E63),
                    iconText = "D",
                    title = "DuitNow QR",
                    subtitle = "Scan QR code",
                    onClick = { showQRDialog = true }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Cash Card
                PaymentOptionCard(
                    icon = Icons.Default.Payment,
                    title = "Cash",
                    subtitle = "Pay with cash",
                    onClick = { showCashConfirm = true }
                )

                Spacer(modifier = Modifier.height(60.dp))

                // Centered TOTAL Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONTINUE",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Cash Confirmation
    if (showCashConfirm) {
        AlertDialog(
            onDismissRequest = { showCashConfirm = false },
            title = { Text(text = "Confirm Cash Payment") },
            text = { Text(text = "Pay $totalAmount with cash?") },
            confirmButton = {
                TextButton(onClick = {
                    showCashConfirm = false
                    onPaymentConfirmed("cash")
                }) {
                    Text(text = "CONFIRM")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCashConfirm = false }) {
                    Text(text = "CANCEL")
                }
            }
        )
    }

    // QR Dialog
    if (showQRDialog) {
        Dialog(onDismissRequest = { showQRDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Scan QR Code",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B87C0)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "QR Code\nDatabase",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = {
                            showQRDialog = false
                            onPaymentConfirmed("qr")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B87C0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "PAY WITH QR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentOptionCard(
    iconBgColor: Color? = null,
    iconText: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color(0xFF4CAF50)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = iconBgColor!!,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Text(
                        text = iconText,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentMethodPreview() {
    MaterialTheme {
        PaymentMethodScreen()
    }
}
