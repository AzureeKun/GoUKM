package com.example.goukm.booking

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goukm.R

@Composable
fun RideRequestCard(request: RideRequestModel, onSkip: () -> Unit, onOffer: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            // Avatar
            Image(
                painter = painterResource(
                    id = request.customerImageRes ?: R.drawable.ic_account_circle_24
                ),
                contentDescription = "Customer avatar",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.width(12.dp))
            Column {
                Text(request.customerName, fontWeight = FontWeight.Bold)
                Text("Pickup: ${request.pickupPoint}")
                Text("Drop-off: ${request.dropOffPoint}")
                Text("${request.seats}-seater ride")
                Text(request.requestedTimeAgo, fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Column {
                Button(onClick = onOffer, colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)) {
                    Text("Offer")
                }
                TextButton(onClick = onSkip) {
                    Text("Skip", color = Color.DarkGray)
                }
            }
        }
    }
}