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
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.text.style.TextAlign
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
                
                // If not found in users, check driverApplications
                if (qrCodeUrl == null) {
                    val appSnapshot = firestore.collection("driverApplications").document(driverId).get().await()
                    val documents = appSnapshot.get("documents") as? Map<*, *>
                    qrCodeUrl = documents?.get("bank_qr") as? String
                }
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
            .background(Color(0xFFF8F9FA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Scan to Pay",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Text(
            text = "DuitNow QR",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // QR Code Card
        Card(
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Keep it square and large
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFF6B87C0))
                } else if (qrCodeUrl != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = qrCodeUrl
                            ),
                            contentDescription = "Driver QR Code",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "DuitNow QR",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFED2C27), // DuitNow red color
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    // Fallback if no QR found
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "QR Code Placeholder",
                            modifier = Modifier.size(140.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No QR code found for this driver",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Please ask driver for payment details or pay via Cash",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "RM ${fetchedAmount ?: totalAmount}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B87C0)
        )
        
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
                .height(60.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B87C0)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                "I've Completed Payment",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

