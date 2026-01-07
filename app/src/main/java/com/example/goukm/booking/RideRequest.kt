package com.example.goukm.booking

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.goukm.R

private val PrimaryBlue = Color(0xFF6B87C0)

@Composable
fun RideRequestCard(
    request: RideRequestModel, 
    onSkip: () -> Unit, 
    onOffer: (() -> Unit)? = null,
    onChat: (() -> Unit)? = null,
    onArrive: (() -> Unit)? = null,
    skipLabel: String = "Skip"
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = PrimaryBlue.copy(alpha = 0.15f)
            )
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // TOP ROW: avatar + Name + Time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F4F8))
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                 ) {
                     if (request.customerProfileUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(request.customerProfileUrl),
                            contentDescription = "Customer",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                     } else {
                        Image(
                            painter = painterResource(id = request.customerImageRes),
                            contentDescription = "Customer",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                     }
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF2D3748)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = request.requestedTimeAgo,
                            fontSize = 12.sp,
                            color = Color(0xFF718096),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (request.paymentMethod == "CASH") Color(0xFFFFF9C4) else Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = request.paymentMethod,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (request.paymentMethod == "CASH") Color(0xFFF57F17) else Color(0xFF2E7D32)
                            )
                        }
                    }
                }
                
                // Display Offered Fare if present
                if (request.offeredFare.isNotEmpty()) {
                     Box(
                         modifier = Modifier
                             .background(
                                 color = Color(0xFFE3F2FD),
                                 shape = RoundedCornerShape(8.dp)
                             )
                             .padding(horizontal = 10.dp, vertical = 6.dp)
                     ) {
                         Text(
                            text = "RM ${request.offeredFare}",
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                         )
                     }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Pickup / Dropoff Timeline
            Row(modifier = Modifier.fillMaxWidth()) {
                // Timeline Line
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 4.dp, end = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(PrimaryBlue, CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(30.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(PrimaryBlue, Color(0xFFFF6B6B))
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFFFF6B6B), CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
                
                // Addresses
                Column(modifier = Modifier.weight(1f)) {
                    Column {
                        Text(
                            text = "Pickup",
                            fontSize = 12.sp,
                            color = Color(0xFF718096),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = request.pickupPoint,
                            fontSize = 14.sp,
                            color = Color(0xFF2D3748),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Drop-off",
                            fontSize = 12.sp,
                            color = Color(0xFF718096),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = request.dropOffPoint,
                            fontSize = 14.sp,
                            color = Color(0xFF2D3748),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
                
                // Seats indicator
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${request.seats}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF4A5568)
                    )
                    Text(
                        text = "seats",
                        fontSize = 10.sp,
                        color = Color(0xFF718096)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // BOTTOM BUTTON ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Chat button for Accepted bookings
                if (onChat != null) {
                    IconButton(
                        onClick = onChat,
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Chat",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                
                // Skip / Cancel
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF718096),
                        containerColor = Color.Transparent
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Text(skipLabel, fontWeight = FontWeight.SemiBold)
                }
                
                // Main Action (Offer or Arrive)
                if (onOffer != null) {
                    Button(
                        onClick = onOffer,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD93D),
                            contentColor = Color.Black
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text("Offer", fontWeight = FontWeight.Bold)
                    }
                }
                
                if (onArrive != null) {
                    Button(
                        onClick = onArrive,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = !request.driverArrived,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (request.driverArrived) Color.Gray else Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            if (request.driverArrived) "Arrived" else "Arrive", 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}