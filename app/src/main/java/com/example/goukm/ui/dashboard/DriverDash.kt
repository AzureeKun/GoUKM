package com.example.goukm.ui.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.goukm.R
import com.example.goukm.booking.RideRequestCard
import com.example.goukm.booking.RideRequestModel
import com.example.goukm.ui.chat.ChatRepository
import com.example.goukm.ui.register.AuthViewModel
import kotlinx.coroutines.launch
import java.net.URLEncoder
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.collectAsState
import com.example.goukm.ui.userprofile.UserProfile

private val PrimaryBlue = Color(0xFF6B87C0)
private val DarkBlue = Color(0xFF4A6199)
private val LightBlue = Color(0xFF8BA3D4)
private val SurfaceColor = Color(0xFFF5F7FB)
private val OnlineGreen = Color(0xFF4CAF50)
private val OfflineRed = Color(0xFFEF5350)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboard(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    selectedNavIndex: Int,
    onNavSelected: (Int) -> Unit
) {
    // Access user profile
    val user by authViewModel.currentUser.collectAsState()
    val isOnline = user?.isAvailable ?: false

    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Real-time Data
    val bookingRepository = remember { com.example.goukm.ui.booking.BookingRepository() }
    var rideRequests by remember { mutableStateOf<List<RideRequestModel>>(emptyList()) }
    var offeredRequests by remember { mutableStateOf<List<RideRequestModel>>(emptyList()) }
    var acceptedRequests by remember { mutableStateOf<List<RideRequestModel>>(emptyList()) }

    DisposableEffect(isOnline) {
        if (isOnline) {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUserId = auth.currentUser?.uid ?: ""
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val listener = db.collection("bookings")
                .whereIn("status", listOf("PENDING", "OFFERED", "ACCEPTED", "ONGOING"))
                .addSnapshotListener { snapshots, e ->
                    if (e != null || snapshots == null) return@addSnapshotListener

                    scope.launch {
                        suspend fun mapToModel(doc: com.google.firebase.firestore.DocumentSnapshot): RideRequestModel? {
                            val pickup = doc.getString("pickup") ?: ""
                            val dropOff = doc.getString("dropOff") ?: ""
                            val seatType = doc.getString("seatType") ?: "4"
                            val seats = seatType.filter { it.isDigit() }.toIntOrNull() ?: 4
                            val userId = doc.getString("userId") ?: ""
                            val timestamp = doc.getDate("timestamp")
                            val offeredFare = doc.getString("offeredFare") ?: ""
                            val pickupLat = doc.getDouble("pickupLat") ?: 0.0
                            val pickupLng = doc.getDouble("pickupLng") ?: 0.0
                            val paymentMethod = doc.getString("paymentMethod") ?: "CASH"
                            
                            val timeAgo = if (timestamp != null) {
                                val diff = java.util.Date().time - timestamp.time
                                val min = diff / 60000
                                if (min < 1) "Just now" else "$min min ago"
                            } else "Just now"

                            val userProfile = com.example.goukm.ui.userprofile.UserProfileRepository.getUserProfile(userId)
                            val name = userProfile?.name ?: "Passenger"
                            
                            val status = doc.getString("status")
                            val driverArrived = doc.getBoolean("driverArrived") ?: false
                            var chatRoom: com.example.goukm.ui.chat.ChatRoom? = null
                            if (status == "ACCEPTED" || status == "ONGOING") {
                                val result = ChatRepository.getChatRoomByBookingId(doc.id)
                                chatRoom = result.getOrNull()
                            }
                            
                            return RideRequestModel(
                                id = doc.id,
                                customerName = name,
                                pickupPoint = pickup,
                                dropOffPoint = dropOff,
                                seats = seats,
                                requestedTimeAgo = timeAgo,
                                customerImageRes = R.drawable.ic_account_circle_24,
                                offeredFare = offeredFare,
                                pickupLat = pickupLat,
                                pickupLng = pickupLng,
                                chatRoom = chatRoom,
                                driverArrived = driverArrived,
                                customerProfileUrl = userProfile?.profilePictureUrl,
                                paymentMethod = paymentMethod,
                                status = status ?: ""
                            )
                        }

                        val pendingList = mutableListOf<RideRequestModel>()
                        val offeredList = mutableListOf<RideRequestModel>()
                        val acceptedList = mutableListOf<RideRequestModel>()

                        for (doc in snapshots.documents) {
                            val status = doc.getString("status") ?: ""
                            val driverId = doc.getString("driverId") ?: ""
                            val offeredDriverIds = doc.get("offeredDriverIds") as? List<String> ?: emptyList()
                            val model = mapToModel(doc) ?: continue
                            
                            when (status) {
                                "ACCEPTED", "ONGOING" -> {
                                    if (driverId == currentUserId) acceptedList.add(model)
                                }
                                "OFFERED" -> {
                                    if (offeredDriverIds.contains(currentUserId)) offeredList.add(model)
                                    else pendingList.add(model)
                                }
                                "PENDING" -> pendingList.add(model)
                            }
                        }
                        rideRequests = pendingList
                        offeredRequests = offeredList
                        acceptedRequests = acceptedList
                    }
                }
            onDispose { listener.remove() }
        } else {
            rideRequests = emptyList()
            offeredRequests = emptyList()
            acceptedRequests = emptyList()
            onDispose { }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC), // Unified Modern Gray-White
        floatingActionButton = {
            AnimatedVisibility(
                visible = isOnline,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                androidx.compose.material3.FloatingActionButton(
                    onClick = { authViewModel.setDriverAvailability(false) },
                    containerColor = Color(0xFFEF5350), // Soft Red
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp) // Slightly larger for emphasis
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Go Offline",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            
            // Header: Only show when "Online" or let it persist but change state? 
            // Let's persist it for a "Modern" app feel.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 20.dp) 
                    // No heavy shadow, just white cleanliness. Or maybe a very subtle 1dp border
                    .shadow(elevation = 1.dp, spotColor = Color.Black.copy(alpha = 0.05f))
            ) {
                 // Left: Greeting / Status
                Column(
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Text(
                        "Hello, ${user?.name ?: "Driver"}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                     Spacer(Modifier.height(4.dp))
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         if (isOnline) {
                             BlinkingDot()
                             Spacer(Modifier.width(8.dp))
                             Text("Online & Searching", fontSize = 12.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.Medium)
                         } else {
                             Box(Modifier.size(6.dp).background(Color.Gray, CircleShape))
                             Spacer(Modifier.width(8.dp))
                             Text("Offline", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                         }
                     }
                }


                // Right: Settings
                IconButton(
                    onClick = { 
                        navController.navigate(com.example.goukm.navigation.NavRoutes.DriverProfile.route) 
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(44.dp)
                        .background(Color(0xFFF1F5F9), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            
            Crossfade(
                targetState = isOnline,
                label = "OnlineStateCrossfade",
                animationSpec = tween(durationMillis = 400)
            ) { online ->
                if (online) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(20.dp)
                    ) {
                        // ACCEPTED JOBS
                        if (acceptedRequests.isNotEmpty()) {
                            item { SectionHeader("Ongoing Trip") }
                            items(acceptedRequests, key = { it.id }) { request ->
                                 RideRequestCard(
                                    request = request,
                                    onSkip = { 
                                        scope.launch {
                                             val encodedAddress = android.net.Uri.encode(request.pickupPoint)
                                             if (request.status == "ONGOING") {
                                                 navController.navigate("driver_journey_summary/${request.id}")
                                             } else {
                                                 navController.navigate("driver_navigation_screen/${request.pickupLat}/${request.pickupLng}/$encodedAddress/${request.id}")
                                             }
                                        }
                                    },
                                    onOffer = null,
                                    skipLabel = "Navigate",
                                    onChat = if (request.chatRoom != null) {
                                        {
                                            val encodedName = URLEncoder.encode(request.chatRoom.customerName, "UTF-8")
                                            val encodedPhone = URLEncoder.encode(request.chatRoom.customerPhone, "UTF-8")
                                            navController.navigate("driver_chat/${request.chatRoom.id}/$encodedName/$encodedPhone")
                                        }
                                    } else null,
                                    onArrive = null 
                                 )
                            }
                        }
    
                        // OFFERED JOBS
                        if (offeredRequests.isNotEmpty() && acceptedRequests.isEmpty()) {
                            item { SectionHeader("Your Offers") }
                             items(offeredRequests, key = { it.id }) { request ->
                                 RideRequestCard(
                                    request = request,
                                    onSkip = {
                                        scope.launch {
                                             bookingRepository.updateStatus(request.id, com.example.goukm.ui.booking.BookingStatus.CANCELLED_BY_DRIVER)
                                                .onSuccess {
                                                    android.widget.Toast.makeText(context, "Offer Cancelled", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    },
                                    onOffer = null,
                                    skipLabel = "Cancel Offer"
                                 )
                            }
                        }
    
                        // NEW REQUESTS
                         if (rideRequests.isNotEmpty() && acceptedRequests.isEmpty() && offeredRequests.isEmpty()) {
                            item { SectionHeader("New Requests") }
                            items(rideRequests, key = { it.id }) { request ->
                                RideRequestCard(
                                    request = request,
                                    onSkip = {
                                        scope.launch {
                                            bookingRepository.updateStatus(request.id, com.example.goukm.ui.booking.BookingStatus.CANCELLED_BY_DRIVER)
                                                .onSuccess {
                                                    android.widget.Toast.makeText(context, "Booking Skipped", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    },
                                    onOffer = {
                                        scope.launch {
                                            bookingRepository.updateStatus(request.id, com.example.goukm.ui.booking.BookingStatus.OFFERED)
                                                .onSuccess {
                                                    navController.navigate(
                                                        "fare_offer/${request.customerName}/${request.pickupPoint}/${request.dropOffPoint}/${request.seats}/${request.id}"
                                                    )
                                                }
                                        }
                                    }
                                )
                            }
                         } else if (rideRequests.isEmpty() && acceptedRequests.isEmpty() && offeredRequests.isEmpty()) {
                             item {
                                 ScanningStateView()
                             }
                         }
                    }
                } else {
                    // Fun/Modern Offline State
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // "Start Engine" / Go Online Button
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .shadow(
                                         elevation = 20.dp, 
                                         shape = CircleShape, 
                                         spotColor = Color(0xFF22C55E).copy(alpha = 0.4f)
                                    )
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF4ADE80), Color(0xFF22C55E))
                                        ), 
                                        CircleShape
                                    )
                                    .clickable { authViewModel.setDriverAvailability(true) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        // Using a standard material icon or we can use painterResource if we had a power icon
                                        // Using PlayArrow as strict replacement for "Start"
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Go Online",
                                        tint = Color.White,
                                        modifier = Modifier.size(60.dp)
                                    )
                                    Text("GO ONLINE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(40.dp))
                            
                            Text(
                                "Ready to roll?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tap the button to start receiving rides.",
                                fontSize = 16.sp,
                                color = Color(0xFF64748B),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            TextButton(
                               onClick = {
                                    scope.launch {
                                        authViewModel.switchActiveRole("customer")
                                        navController.navigate("customer_dashboard") {
                                            popUpTo("driver_dashboard") { inclusive = true }
                                        }
                                    }
                               }
                            ) {
                                Text("Or switch to Customer Mode", color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp, // Slightly bigger, not caps
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E293B),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
fun ScanningStateView() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanningRipple")
    
    // Animation for the first ripple
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "scale1"
    )
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "alpha1"
    )

    // Animation for the second ripple (just a slightly different timing or size if we could, 
    // but simplified: we'll just layer a second one with a static offset modifier or accept one pulsing ring for simplicity & performance,
    // OR just use a second independent transition with initial delay if we were in a LaunchedEffect, but infiniteTransition runs immediately.
    // Let's stick to one nice large pulse + a static inner glow to keep it clean.)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
             // Pulsing Ripple
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = scale1
                        scaleY = scale1
                        alpha = alpha1
                    }
                    .background(Color(0xFF0EA5E9), CircleShape)
            )
            
             // Static Inner Glow
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFFE0F2FE).copy(alpha = 0.3f), CircleShape)
            )
            
            // Icon
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF0EA5E9),
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Scanning area...",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Relax, we'll ping you when a ride appears!",
            fontSize = 15.sp,
            color = Color(0xFF64748B),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun BlinkingDot() {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(1000),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        )
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(Color(0xFF22C55E).copy(alpha = alpha), CircleShape)
    )
}

// Extension to help with scaling effect for switch
fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)

