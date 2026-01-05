package com.example.goukm.ui.chat

import com.example.goukm.ui.theme.CBlue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.goukm.navigation.NavRoutes
import com.example.goukm.ui.dashboard.BottomNavigationBarDriver
import com.google.firebase.auth.FirebaseAuth
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverChatListScreen(navController: NavHostController) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val chatRooms = remember { mutableStateListOf<ChatRoom>() }
    var isLoading by remember { mutableStateOf(true) }
    var selectedNavIndex by remember { mutableStateOf(-1) }

    // Listen to chat rooms in real-time
    LaunchedEffect(Unit) {
        ChatRepository.getChatRoomsForUser().collect { rooms ->
            chatRooms.clear()
            // Filter to only show rooms where current user is the driver
            chatRooms.addAll(rooms.filter { it.driverId == currentUserId })
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Messages",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CBlue)
            )
        },
        bottomBar = {
            BottomNavigationBarDriver(
                selectedIndex = selectedNavIndex,
                onSelected = { index ->
                    selectedNavIndex = index
                    when (index) {
                        0 -> navController.navigate(NavRoutes.DriverDashboard.route) {
                            popUpTo(NavRoutes.DriverDashboard.route) { inclusive = true }
                        }
                        1 -> navController.navigate(NavRoutes.DriverScore.route)
                        2 -> navController.navigate(NavRoutes.DriverEarning.route)
                        3 -> navController.navigate(NavRoutes.DriverProfile.route)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CBlue)
                    }
                }
                chatRooms.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Message,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = Color.LightGray
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No messages yet",
                                fontSize = 18.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Chat will appear when you accept a ride",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(chatRooms) { chatRoom ->
                            ChatRoomListItem(
                                chatRoom = chatRoom,
                                contactName = chatRoom.customerName,
                                unreadCount = chatRoom.driverUnreadCount,
                                onClick = {
                                    val encodedName = URLEncoder.encode(chatRoom.customerName, "UTF-8")
                                    val encodedPhone = URLEncoder.encode(chatRoom.customerPhone, "UTF-8")
                                    navController.navigate("driver_chat/${chatRoom.id}/$encodedName/$encodedPhone")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
