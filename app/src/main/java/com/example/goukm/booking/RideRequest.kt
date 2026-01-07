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
import androidx.compose.material.icons.filled.DirectionsCar
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
        shape = RoundedCornerShape(24.dp), // Increased radius
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 6.dp, // slightly softer elevation
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.08f) // soft shadow
            )
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // TOP ROW: Avatar + Name + Payment
            Row(verticalAlignment = Alignment.Top) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                         .clip(CircleShape)
                         .background(Color(0xFFF0F4F8)),
                    contentAlignment = Alignment.Center
                 ) {
                     if (request.customerProfileUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(request.customerProfileUrl),
                            contentDescription = "Customer",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                     } else {
                        Image(
                            painter = painterResource(id = request.customerImageRes),
                            contentDescription = "Customer",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                     }
                }
                
                Spacer(Modifier.width(16.dp))
                
                // Name & Time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         // Payment Pill
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (request.paymentMethod == "CASH") Color(0xFFFFF9C4) else Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = request.paymentMethod,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (request.paymentMethod == "CASH") Color(0xFFF57F17) else Color(0xFF2E7D32)
                            )
                        }
                        
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = request.requestedTimeAgo,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                
                // Fare Pill (if available)
                if (request.offeredFare.isNotEmpty()) {
                     Box(
                         modifier = Modifier
                             .background(
                                 color = Color(0xFFEEF2FF),
                                 shape = RoundedCornerShape(12.dp)
                             )
                             .padding(horizontal = 12.dp, vertical = 8.dp)
                     ) {
                         Text(
                            text = "RM${request.offeredFare}",
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                         )
                     }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Pickup / Dropoff Timeline
            Row(modifier = Modifier.fillMaxWidth()) {
                // Modern Timeline
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 2.dp, end = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                             .border(2.dp, PrimaryBlue, CircleShape)
                             .background(Color.White, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp) // Thinner line
                            .height(30.dp)
                            .background(Color(0xFFE2E8F0)) // Very light gray line
                    )
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(14.dp)
                    )
                }
                
                // Address Text
                Column(modifier = Modifier.weight(1f)) {
                    // Pickup
                    Column {
                        Text(
                            text = "PICKUP",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                         Spacer(Modifier.height(2.dp))
                        Text(
                            text = request.pickupPoint,
                            fontSize = 15.sp,
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    // Dropoff
                    Column {
                         Text(
                            text = "DROPOFF",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = request.dropOffPoint,
                            fontSize = 15.sp,
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
                
                // Seats
                Column(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                         imageVector = Icons.Default.DirectionsCar, 
                         contentDescription = null,
                         tint = Color(0xFFCBD5E1),
                         modifier = Modifier.size(20.dp)
                    )
                     Text(
                        text = "${request.seats}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // BOTTOM BUTTON ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                 // Chat button
                if (onChat != null) {
                    IconButton(
                        onClick = onChat,
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFF1F5F9), CircleShape) // Circular button
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Chat",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Skip Button
                Button(
                    onClick = onSkip,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(50), // Fully rounded
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF1F5F9), // Very light gray
                        contentColor = Color(0xFF64748B)    // Slate gray text
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(skipLabel, fontWeight = FontWeight.SemiBold)
                }
                
                // Action Button (Offer/Arrive)
                if (onOffer != null || onArrive != null) {
                    val isArriveMode = onArrive != null
                    val btnColor = if (isArriveMode) {
                        if (request.driverArrived) Color(0xFFCBD5E1) else Color(0xFF22C55E) // Green for Arrive
                    } else {
                        Color(0xFF3B82F6) // Blue for Offer (Aesthetic)
                    }
                    val btnText = if (isArriveMode) {
                         if (request.driverArrived) "Arrived" else "Arrive"
                    } else "Make Offer"

                    Button(
                        onClick = {
                            if (isArriveMode) onArrive?.invoke()
                            else onOffer?.invoke()
                        },
                        modifier = Modifier
                            .weight(1.5f) // Slightly wider
                            .height(50.dp),
                        shape = RoundedCornerShape(50),
                        enabled = if (isArriveMode) !request.driverArrived else true,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = btnColor,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(btnText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}