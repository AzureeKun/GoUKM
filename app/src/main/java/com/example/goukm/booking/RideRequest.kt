package com.example.goukm.booking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goukm.R

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
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // TOP ROW: avatar + "Ride Request - time"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = request.customerImageRes),
                    contentDescription = "Customer",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.customerName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Row {
                        Text(
                            text = "Ride Request",
                            color = Color(0xFFd9a500),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "  -  ${request.requestedTimeAgo}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                // Display Offered Fare if present
                if (request.offeredFare.isNotEmpty()) {
                     Text(
                        text = "RM ${request.offeredFare}",
                        color = Color(0xFFE91E63),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 8.dp)
                     )
                }
                // Chat icon button
                if (onChat != null) {
                    IconButton(
                        onClick = onChat,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Chat",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // MIDDLE: pickup -> drop-off row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pickup Point",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                         color = Color.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Pickup",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = request.pickupPoint,
                            fontSize = 11.sp,
                            color = Color.Black
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "to",
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(18.dp),
                    tint = Color.Black
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Drop-Off Point",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PersonPinCircle,
                            contentDescription = "Dropoff",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = request.dropOffPoint,
                            fontSize = 11.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // CENTER TEXT: seat type
            Text(
                text = "${request.seats} seater ride",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black
            )

            Spacer(Modifier.height(8.dp))

            // BOTTOM BUTTON ROW: Skip | Offer | Arrive
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.DarkGray
                    )
                ) {
                    Text(skipLabel)
                }
                
                if (onOffer != null) {
                    Button(
                        onClick = onOffer,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFEB3B),
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Offer")
                    }
                }
                
                if (onArrive != null) {
                    Button(
                        onClick = onArrive,
                        modifier = Modifier.weight(1f),
                        enabled = !request.driverArrived,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (request.driverArrived) Color.Gray else Color(0xFF4CAF50), // Main Green
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (request.driverArrived) "Arrived" else "Arrive")
                    }
                }
            }
        }
    }
}