package com.example.goukm.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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

    // Removed local LaunchedEffect for profile/FCM as AuthViewModel handles it

    // Real-time Data
    val bookingRepository = remember { com.example.goukm.ui.booking.BookingRepository() }
    var rideRequests by remember { mutableStateOf<List<RideRequestModel>>(emptyList()) }
    var offeredRequests by remember { mutableStateOf<List<RideRequestModel>>(emptyList()) }
    var acceptedRequests by remember { mutableStateOf<List<RideRequestModel>>(emptyList()) }

    DisposableEffect(isOnline) {
        if (isOnline) {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUserId = auth.currentUser?.uid
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
                                    // STRICT CHECK: Only show if I am the assigned driver
                                    if (driverId == currentUserId) {
                                        acceptedList.add(model)
                                    }
                                }
                                "OFFERED" -> {
                                    // If I have offered, show in Offered list
                                    if (offeredDriverIds.contains(currentUserId)) {
                                        offeredList.add(model)
                                    } else {
                                        // If I haven't offered yet, it's still a pending request for me
                                        pendingList.add(model)
                                    }
                                }
                                "PENDING" -> {
                                    // Regular pending request
                                    pendingList.add(model)
                                }
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
        containerColor = SurfaceColor,
        bottomBar = {
            BottomNavigationBarDriver(
                selectedIndex = selectedNavIndex,
                onSelected = onNavSelected
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Modern Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, spotColor = Color.Black.copy(alpha = 0.1f))
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOnline) "You are Online" else "You are Offline",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOnline) OnlineGreen else Color.Gray
                        )
                        user?.let { u ->
                            if (u.vehiclePlateNumber.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(PrimaryBlue.copy(alpha=0.1f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "In Use: ${u.carBrand} • ${u.vehiclePlateNumber}",
                                        fontSize = 13.sp,
                                        color = PrimaryBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                    
                    Switch(
                        checked = isOnline,
                        onCheckedChange = {
                            authViewModel.setDriverAvailability(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OnlineGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = OfflineRed
                        ),
                        modifier = Modifier.scale(0.9f)
                    )

                }
            }
            
            
            if (isOnline) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
                ) {
                    // ACCEPTED JOBS
                    if (acceptedRequests.isNotEmpty()) {
                        item {
                            SectionHeader("Current Job", OnlineGreen)
                        }
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
                        item {
                            SectionHeader("Awaiting Customer Response", Color(0xFFFFA000))
                        }
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
                        item {
                            SectionHeader("New Requests", PrimaryBlue)
                        }
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
                             EmptyStateView(
                                 message = "No new requests right now",
                                 subMessage = "Stay online to receive ride request"
                             )
                         }
                     }
                }
            } else {
                // Offline State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.carroad),
                            contentDescription = "Offline",
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            "You are currently Offline",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color(0xFF2D3748)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Go online to start receiving ride requests",
                            fontSize = 16.sp,
                            color = Color(0xFF718096),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = { authViewModel.setDriverAvailability(true) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OnlineGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Go Online", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    authViewModel.switchActiveRole("customer")
                                    navController.navigate("customer_dashboard") {
                                        popUpTo("driver_dashboard") { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Switch to Customer Mode", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 24.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748)
        )
    }
}

@Composable
fun EmptyStateView(message: String, subMessage: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFFE3F2FD), CircleShape)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.List,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(60.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            message,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            subMessage,
            fontSize = 14.sp,
            color = Color(0xFF718096)
        )
    }
}

// Extension to help with scaling effect for switch
fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)

