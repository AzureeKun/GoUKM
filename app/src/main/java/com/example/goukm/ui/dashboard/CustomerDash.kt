package com.example.goukm.ui.dashboard

import com.example.goukm.ui.theme.CBlue

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.goukm.navigation.NavRoutes
import com.example.goukm.ui.chat.ChatRepository
import com.example.goukm.ui.chat.ChatRoom
import com.example.goukm.ui.booking.RecentPlace
import com.example.goukm.ui.booking.RecentPlaceRepository
import kotlinx.coroutines.launch
import java.util.Calendar

// Modern color palette based on CBlue
private val PrimaryBlue = Color(0xFF6B87C0)
private val DarkBlue = Color(0xFF4A6199)
private val LightBlue = Color(0xFF8BA3D4)
private val SurfaceColor = Color(0xFFF5F7FB)
private val CardSurface = Color(0xFFFFFFFF)
private val AccentPink = Color(0xFFFFE4E4)
private val AccentLightBlue = Color(0xFFDCE6FF)


@Composable
fun CustomerDashboard(
    navController: NavHostController,
    userImageUrl: String? = null
) {
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    val db = remember { com.google.firebase.firestore.FirebaseFirestore.getInstance() }
    var activeBooking by remember { mutableStateOf<com.example.goukm.ui.booking.Booking?>(null) }
    var chatRoom by remember { mutableStateOf<ChatRoom?>(null) }
    var recentPlaces by remember { mutableStateOf<List<RecentPlace>>(emptyList()) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Get time-based greeting
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    LaunchedEffect(activeBooking?.driverArrived) {
        if (activeBooking?.driverArrived == true) {
            android.widget.Toast.makeText(context, "Driver has arrived!", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        recentPlaces = RecentPlaceRepository.getRecentPlaces()
    }

    LaunchedEffect(activeBooking) {
        if (activeBooking != null && (activeBooking!!.status == "ACCEPTED" || activeBooking!!.status == "ONGOING")) {
            val result = ChatRepository.getChatRoomByBookingId(activeBooking!!.id)
            chatRoom = result.getOrNull()
        } else {
            chatRoom = null
        }
    }

    DisposableEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val listener = db.collection("bookings")
                .whereEqualTo("userId", currentUser.uid)
                .whereIn("status", listOf("PENDING", "OFFERED", "ACCEPTED", "ONGOING"))
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
                            driverId = doc.getString("driverId") ?: "",
                            driverArrived = doc.getBoolean("driverArrived") ?: false
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

    Scaffold(
        containerColor = SurfaceColor
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Modern Gradient Header with curved bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(DarkBlue, PrimaryBlue, LightBlue)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            greeting,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Where to?",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Profile Avatar with glow effect
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                spotColor = Color.White.copy(alpha = 0.4f)
                            )
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .padding(3.dp)
                    ) {
                        if (userImageUrl != null) {
                            Image(
                                painter = rememberAsyncImagePainter(userImageUrl),
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Active Booking Banner
            if (activeBooking != null) {
                Spacer(modifier = Modifier.height(16.dp))
                ActiveBookingCard(
                    activeBooking = activeBooking!!,
                    chatRoom = chatRoom,
                    navController = navController
                )
            }

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Feature Cards Section
                Text(
                    "Quick Actions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkBlue
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ModernFeatureCard(
                        title = "Book a Ride",
                        subtitle = "Find your driver",
                        icon = Icons.Outlined.DirectionsCar,
                        gradientColors = listOf(Color(0xFFFF9A9E), Color(0xFFFECFEF)),
                        onClick = { 
                            if (activeBooking != null && (activeBooking?.status == "PENDING" || activeBooking?.status == "OFFERED" || activeBooking?.status == "ACCEPTED")) {
                                navController.navigate("booking_request?bookingId=${activeBooking?.id}")
                            } else {
                                navController.navigate("booking_request") 
                            }
                        }
                    )
                    ModernFeatureCard(
                        title = "History",
                        subtitle = "Past rides",
                        icon = Icons.Outlined.History,
                        gradientColors = listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
                        onClick = { navController.navigate(NavRoutes.CustomerBookingHistory.route) }
                    )
                }

                // Recent Places Section
                Text(
                    "Recent Places",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkBlue
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        if (recentPlaces.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No recent places yet",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            recentPlaces.forEach { place ->
                                RecentPlaceRow(
                                    text = place.name,
                                    onClick = { 
                                        navController.navigate("booking_request")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveBookingCard(
    activeBooking: com.example.goukm.ui.booking.Booking,
    chatRoom: ChatRoom?,
    navController: NavHostController
) {
    val status = activeBooking.status
    val isOffered = status == "OFFERED"
    val isAccepted = status == "ACCEPTED"
    val isOngoing = status == "ONGOING"
    val canChat = isAccepted || isOngoing

    val cardColors = when {
        isOngoing -> listOf(Color(0xFF00B4DB), Color(0xFF0083B0))
        isAccepted -> listOf(Color(0xFF56AB2F), Color(0xFFA8E063))
        isOffered -> listOf(Color(0xFF667EEA), Color(0xFF764BA2))
        else -> listOf(Color(0xFFFFD93D), Color(0xFFFF6B6B))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        onClick = {
            if (isOffered) {
                navController.navigate("booking_request?bookingId=${activeBooking.id}")
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(cardColors)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        when {
                            activeBooking.driverArrived -> "🚗 Driver Arrived!"
                            isOngoing -> "🚙 Ride in Progress"
                            isAccepted -> "✅ Ride Confirmed"
                            isOffered -> "💰 Offer Received"
                            else -> "🔍 Finding Driver..."
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        when {
                            isOngoing -> "Enjoy your ride!"
                            isAccepted -> "Driver is on the way"
                            isOffered -> "Tap to view options"
                            else -> "Please wait"
                        },
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (canChat && chatRoom != null) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .clickable {
                                    val encodedName = java.net.URLEncoder.encode(chatRoom.driverName, "UTF-8")
                                    val encodedPhone = java.net.URLEncoder.encode(chatRoom.driverPhone, "UTF-8")
                                    navController.navigate("customer_chat/${chatRoom.id}/$encodedName/$encodedPhone")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ChatBubble,
                                contentDescription = "Chat",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isOffered -> Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "View",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            isAccepted || isOngoing -> Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Active",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            else -> CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentPlaceRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(AccentLightBlue, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Place,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text,
            color = Color(0xFF2D3748),
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ArrowForward,
            contentDescription = null,
            tint = Color(0xFFCBD5E0),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun RowScope.ModernFeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100)
    )

    Card(
        modifier = Modifier
            .weight(1f)
            .height(140.dp)
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        onClick = { onClick?.invoke() },
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(gradientColors)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        subtitle,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}