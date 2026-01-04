package com.example.goukm.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/* ------------------------------------------------ */
/* MAIN SCREEN (LOGIC + DIALOGS – NOT PREVIEWED)     */
/* ------------------------------------------------ */

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
                        fontSize = 22.sp, // reduced for layout safety
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF6B87C0)
                )
            )
        }
    ) { padding ->
        PaymentMethodContent(
            modifier = Modifier.padding(padding),
            onCashClick = { showCashConfirm = true },
            onQrClick = { showQRDialog = true }
        )
    }

    /* -------------------- CASH CONFIRM -------------------- */

    if (showCashConfirm) {
        AlertDialog(
            onDismissRequest = { showCashConfirm = false },
            title = { Text("Confirm Cash Payment") },
            text = { Text("Pay $totalAmount with cash?") },
            confirmButton = {
                TextButton(onClick = {
                    showCashConfirm = false
                    onPaymentConfirmed("cash")
                }) {
                    Text("CONFIRM")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCashConfirm = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    /* -------------------- QR DIALOG -------------------- */

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

                    Spacer(Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .background(
                                Color(0xFFF5F5F5),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "QR Code\n(Database)",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(28.dp))

                    Button(
                        onClick = {
                            showQRDialog = false
                            onPaymentConfirmed("qr")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6B87C0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("PAY WITH QR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------ */
/* UI CONTENT (PREVIEW SAFE – NO DIALOGS)            */
/* ------------------------------------------------ */

@Composable
fun PaymentMethodContent(
    modifier: Modifier = Modifier,
    onCashClick: () -> Unit = {},
    onQrClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6B87C0),
                        Color(0xFF5A76B0)
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

            PaymentOptionCard(
                iconBgColor = Color(0xFFE91E63),
                iconText = "D",
                title = "DuitNow QR",
                subtitle = "Scan QR code",
                onClick = onQrClick
            )

            Spacer(Modifier.height(24.dp))

            PaymentOptionCard(
                icon = Icons.Default.Payment,
                title = "Cash",
                subtitle = "Pay with cash",
                onClick = onCashClick
            )

            Spacer(Modifier.height(60.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "CONTINUE",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------ */
/* COMPONENT                                         */
/* ------------------------------------------------ */

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
            modifier = Modifier.padding(24.dp),
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
                            iconBgColor ?: Color.Gray,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconText,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            Column {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

/* ------------------------------------------------ */
/* PREVIEW                                           */
/* ------------------------------------------------ */

@Preview(showBackground = true)
@Composable
fun PaymentMethodPreview() {
    MaterialTheme {
        PaymentMethodContent()
    }
}
