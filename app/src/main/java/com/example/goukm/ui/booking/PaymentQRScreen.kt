package com.example.goukm.ui.booking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.runtime.*
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun PaymentQRScreen(
    totalAmount: String,
    bookingId: String,
    onPaymentCompleted: () -> Unit
) {
    var qrCodeUrl by remember { mutableStateOf<String?>(null) }
    var fetchedAmount by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(bookingId) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val bookingSnapshot = firestore.collection("bookings").document(bookingId).get().await()
            val driverId = bookingSnapshot.getString("driverId")
            
            // Try fetch offeredFare (could be String or Double)
            val fareObj = bookingSnapshot.get("offeredFare")
            fetchedAmount = fareObj?.toString()

            if (driverId != null) {
                val driverSnapshot = firestore.collection("users").document(driverId).get().await()
                qrCodeUrl = driverSnapshot.getString("bankQrUrl")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Scan to Pay",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "DuitNow QR",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // QR Code Card
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.size(280.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (qrCodeUrl != null) {
                   Image(
                       painter = rememberAsyncImagePainter(qrCodeUrl),
                       contentDescription = "Driver QR Code",
                       modifier = Modifier.fillMaxSize()
                   )
                } else {
                    // Fallback if no QR found
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "QR Code Placeholder",
                            modifier = Modifier.size(100.dp),
                            tint = Color.Gray
                        )
                        Text("No QR Code Available", fontSize = 12.sp, color = Color.Gray)
                        Text("Pay Cash Instead", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "RM ${fetchedAmount ?: totalAmount}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B87C0)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Please scan the driver's QR code using your banking app.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

            Button(
            onClick = onPaymentCompleted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B87C0))
        ) {
            Text(
                "Payment Completed",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

