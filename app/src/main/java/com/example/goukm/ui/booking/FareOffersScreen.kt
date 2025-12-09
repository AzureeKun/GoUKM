package com.example.goukm.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.goukm.ui.dashboard.BottomBar

data class DriverOffer(
    val name: String,
    val fareLabel: String, // e.g., "RM 5"
    val carBrand: String,
    val carName: String,
    val carColor: String,
    val plate: String
)

@Composable
fun FareOffersScreen(
    navController: NavHostController,
    offers: List<DriverOffer> = sampleOffers,
    seatLabel: String = "4 seater ride",
    pickup: String = "Kolej Aminuddin Baki",
    dropOff: String = "Kolej Pendeta Za'ba"
) {
    val headerBlue = Color(0xFF6B87C0)
    val cardBg = Color.White
    val yellow = Color(0xFFFFD60A)
    val grayBg = Color(0xFFF6F6F6)

    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = headerBlue
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Text(
                    text = "Ride Offer",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                // Trip summary card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(seatLabel, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Pickup Point", fontWeight = FontWeight.Bold)
                        ChipField(
                            leadingIcon = Icons.Default.Send,
                            text = pickup
                        )
                        Text("Drop-Off Point", fontWeight = FontWeight.Bold)
                        ChipField(
                            leadingIcon = Icons.Default.Place,
                            text = dropOff
                        )
                    }
                }

                // Only offers list scrolls
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(offers) { offer ->
                        OfferCard(offer = offer, badgeColor = yellow, cardBg = cardBg, grayBg = grayBg)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipField(leadingIcon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF2F3F5))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(leadingIcon, contentDescription = null, tint = Color.Gray)
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.DarkGray, fontSize = 13.sp)
    }
}

@Composable
private fun OfferCard(
    offer: DriverOffer,
    badgeColor: Color,
    cardBg: Color,
    grayBg: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.width(72.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(grayBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }
                Text(offer.name.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Spacer(Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("Car Brand: ${offer.carBrand}", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                Text("Car Name: ${offer.carName}", fontSize = 12.sp, color = Color.Black)
                Text("Car Color: ${offer.carColor}", fontSize = 12.sp, color = Color.Black)
                Text("Number Plate: ${offer.plate}", fontSize = 12.sp, color = Color.Black)
            }

            Spacer(Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(grayBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(offer.fareLabel, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }
                Button(
                    onClick = { /* TODO: handle accept offer */ },
                    colors = ButtonDefaults.buttonColors(containerColor = badgeColor),
                    modifier = Modifier.width(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Text("Accept", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

private val sampleOffers = listOf(
    DriverOffer(
        name = "Angela Kelly",
        fareLabel = "RM 5",
        carBrand = "Perodua",
        carName = "Axia",
        carColor = "Silver",
        plate = "KFM1044"
    ),
    DriverOffer(
        name = "Farhan Iskandar",
        fareLabel = "RM 7",
        carBrand = "Proton",
        carName = "Persona",
        carColor = "Blue",
        plate = "LA 3456"
    ),
    DriverOffer(
        name = "Fatehh Mustafa",
        fareLabel = "RM 4",
        carBrand = "Honda",
        carName = "Civic",
        carColor = "Red",
        plate = "VFG 1322"
    )
)

