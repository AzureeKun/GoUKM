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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentWidth
import com.example.goukm.ui.booking.BookingStatus
import com.example.goukm.navigation.NavRoutes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.goukm.ui.dashboard.BottomBar
import com.example.goukm.ui.chat.ChatRepository
import com.example.goukm.ui.driver.FareOfferScreen
import com.example.goukm.ui.journey.CustomerJourneyDetailsScreen
import com.google.firebase.auth.FirebaseAuth

data class DriverOffer(
    val name: String,
    val fareLabel: String, // e.g., "RM 5"
    val carBrand: String,
    val carName: String,
    val carColor: String,
    val plate: String,
    val driverId: String = "",
    val driverPhone: String = ""
)

@Composable
fun FareOffersScreen(
    navController: NavHostController,
    bookingId: String
) { // Payment method retrieved from booking
    val headerBlue = Color(0xFF6B87C0)
    val cardBg = Color.White
    val yellow = Color(0xFFFFD60A)
    val grayBg = Color(0xFFF6F6F6)

    var isLoading by remember { mutableStateOf(true) }
    var offer by remember { mutableStateOf<DriverOffer?>(null) }
    var pickup by remember { mutableStateOf("") }
    var dropOff by remember { mutableStateOf("") }
    var seatLabel by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }

    val bookingRepository = remember { com.example.goukm.ui.booking.BookingRepository() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(bookingId) {
        val result = bookingRepository.getBooking(bookingId)
        val booking = result.getOrNull()
        if (booking != null) {
            pickup = booking.pickup
            dropOff = booking.dropOff
            seatLabel = "${booking.seatType.filter { it.isDigit() }} seater ride"
            paymentMethod = booking.paymentMethod

            if (booking.driverId.isNotEmpty()) {
                val driver =
                    com.example.goukm.ui.userprofile.UserProfileRepository.getUserProfile(booking.driverId)
                if (driver != null) {
                    offer = DriverOffer(
                        name = driver.name,
                        fareLabel = "RM ${booking.offeredFare}",
                        carBrand = driver.vehicleType,
                        carName = "",
                        carColor = "",
                        plate = driver.vehiclePlateNumber,
                        driverId = booking.driverId,
                        driverPhone = driver.phoneNumber
                    )
                }
            }
        }
        isLoading = false
    }

    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = headerBlue
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator(color = Color.White)
                }
            } else {
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

                    // Offers list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (offer != null) {
                            item {
                                OfferCard(
                                    offer = offer!!,
                                    badgeColor = yellow,
                                    cardBg = cardBg,
                                    grayBg = grayBg,
                                    onAccept = {
                                        scope.launch {
                                            bookingRepository.updateStatus(
                                                bookingId,
                                                BookingStatus.ACCEPTED
                                            )

                                            val currentUser = FirebaseAuth.getInstance().currentUser
                                            if (currentUser != null && offer != null) {
                                                val customerProfile =
                                                    com.example.goukm.ui.userprofile.UserProfileRepository.getUserProfile(
                                                        currentUser.uid
                                                    )
                                                ChatRepository.createChatRoom(
                                                    bookingId = bookingId,
                                                    customerId = currentUser.uid,
                                                    driverId = offer!!.driverId,
                                                    customerName = customerProfile?.name
                                                        ?: "Customer",
                                                    driverName = offer!!.name,
                                                    customerPhone = customerProfile?.phoneNumber
                                                        ?: "",
                                                    driverPhone = offer!!.driverPhone
                                                )
                                            }

                                            navController.navigate("cust_journey_details/$bookingId/$paymentMethod") {
                                                popUpTo(NavRoutes.CustomerDashboard.route) {
                                                    inclusive = true
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        } else {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Waiting for offers...",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
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
    grayBg: Color,
    onAccept: () -> Unit
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
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = badgeColor),
                    modifier = Modifier.wrapContentWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
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

//@Preview(showBackground = true)
//@Composable
//fun FareOfferScreenPreview() {
//    MaterialTheme {
//        FareOfferScreen(
//
//        )
//    }
//}

