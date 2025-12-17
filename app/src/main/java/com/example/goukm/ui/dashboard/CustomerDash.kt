package com.example.goukm.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.goukm.navigation.NavRoutes
import com.example.goukm.ui.userprofile.CBlue

@Composable
fun BottomBar(navController: NavHostController) {
    NavigationBar(
        containerColor = CBlue
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { /* TODO: Navigate Home */ },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color.White
                )
            },
            label = { Text("Home", color = Color.White) },
            alwaysShowLabel = true
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(NavRoutes.CustomerChatList.route) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = "Chat",
                    tint = Color.White
                )
            },
            label = { Text("Chat", color = Color.White) },
            alwaysShowLabel = true
        )

        NavigationBarItem(
            selected = false, // current screen
            onClick = { navController.navigate(NavRoutes.CustomerProfile.route) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White
                )
            },
            label = { Text("Profile", color = Color.White) },
            alwaysShowLabel = true
        )
    }
}

@Composable
fun CustomerDashboard(
    navController: NavHostController,
    userImageUrl: String? = null
) {
    val headerBlue = Color(0xFF6B87C0)
    val searchBg = Color(0xFFF5F6FA)

    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    val db = remember { com.google.firebase.firestore.FirebaseFirestore.getInstance() }
    var activeBooking by remember { mutableStateOf<com.example.goukm.ui.booking.Booking?>(null) }

    DisposableEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val listener = db.collection("bookings")
                .whereEqualTo("userId", currentUser.uid)
                .whereIn("status", listOf("PENDING", "OFFERED", "ACCEPTED"))
                .addSnapshotListener { snapshot, e ->
                     if (e != null || snapshot == null || snapshot.isEmpty) {
                         activeBooking = null
                         return@addSnapshotListener
                     }
                     val doc = snapshot.documents.firstOrNull()
                     if (doc != null) {
                         activeBooking = com.example.goukm.ui.booking.Booking(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            pickup = doc.getString("pickup") ?: "",
                            dropOff = doc.getString("dropOff") ?: "",
                            seatType = doc.getString("seatType") ?: "",
                            status = doc.getString("status") ?: "",
                            offeredFare = doc.getString("offeredFare") ?: "",
                            driverId = doc.getString("driverId") ?: ""
                         )
                     } else {
                         activeBooking = null
                     }
                }
            onDispose { listener.remove() }
        } else {
            onDispose { }
        }
    }

    Scaffold(bottomBar = { BottomBar(navController) }) { paddingValues ->

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBlue)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dashboard", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            Text("Need a ride today?", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                        if (userImageUrl != null) {
                            Image(
                                painter = rememberAsyncImagePainter(userImageUrl),
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                // Active Booking Banner
                if (activeBooking != null) {
                    val status = activeBooking!!.status
                    val isOffered = status == "OFFERED"
                    val isAccepted = status == "ACCEPTED"
                    
                    val cardColor = when {
                        isAccepted -> Color(0xFFC8E6C9) // Green for Accepted
                        isOffered -> Color(0xFFE0F7FA) // Blueish for Offered
                        else -> Color(0xFFFFF9C4) // Yellow used for Pending
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                         colors = CardDefaults.cardColors(containerColor = cardColor),
                         onClick = {
                             if (isOffered) {
                                 navController.navigate("fare_offers_screen/${activeBooking!!.id}")
                             }
                         }
                    ) {
                        Row(
                             modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                             verticalAlignment = Alignment.CenterVertically,
                             horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    when {
                                        isAccepted -> "You accepted the ride!"
                                        isOffered -> "Driver Offer Received!"
                                        else -> "Finding you a driver..."
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    when {
                                        isAccepted -> "Driver is on the way"
                                        isOffered -> "Tap to view options"
                                        else -> "Please wait"
                                    },
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            }
                            if (isOffered) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "View", tint = Color.Black)
                            } else if (isAccepted) {
                                Icon(Icons.Default.Favorite, contentDescription = "Accepted", tint = Color(0xFF2E7D32))
                            } else {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("History", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)

                    FavouriteRow("Fakulti Teknologi dan Sains Maklumat, Jalan")
                    FavouriteRow("Kolej Pendeta Za'ba - UKM, Jalan Tun Isma")
                    FavouriteRow("Kolej Ibrahim Yaakub - UKM, Jalan Tun Isma")

                    Text("Rides for your every need", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FeatureCard(
                            title = "Book a ride",
                            icon = Icons.Outlined.DirectionsCar,
                            bgColor = Color(0xFFFFE4E4),
                            onClick = { navController.navigate(NavRoutes.BookingRequest.route) }
                        )
                        FeatureCard(
                            title = "Booking history",
                            icon = Icons.Outlined.History,
                            bgColor = Color(0xFFDCE6FF),
                            onClick = { /* TODO: navigate to booking history */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavouriteRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.StarBorder, contentDescription = null, tint = Color.Black)
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RowScope.FeatureCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onClick?.invoke() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = Color.Black)
            Text(title, color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}