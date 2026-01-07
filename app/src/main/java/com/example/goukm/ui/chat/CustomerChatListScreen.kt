package com.example.goukm.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

import com.example.goukm.ui.theme.CBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerChatListScreen(navController: NavHostController) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val chatRooms = remember { mutableStateListOf<ChatRoom>() }
    var isLoading by remember { mutableStateOf(true) }

    // Listen to chat rooms in real-time
    LaunchedEffect(Unit) {
        ChatRepository.getChatRoomsForUser().collect { rooms ->
            chatRooms.clear()
            // Filter to only show rooms where current user is the customer
            chatRooms.addAll(rooms.filter { it.customerId == currentUserId })
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FB)) // SurfaceColor from dashboard
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
                                "Chat will appear when you have an active ride",
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
                                contactName = chatRoom.driverName,
                                unreadCount = chatRoom.customerUnreadCount,
                                onClick = {
                                    val encodedName = URLEncoder.encode(chatRoom.driverName, "UTF-8")
                                    val encodedPhone = URLEncoder.encode(chatRoom.driverPhone, "UTF-8")
                                    navController.navigate("customer_chat/${chatRoom.id}/$encodedName/$encodedPhone")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatRoomListItem(
    chatRoom: ChatRoom,
    contactName: String,
    unreadCount: Int,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    
    val timeString = remember(chatRoom.lastMessageTime) {
        val now = System.currentTimeMillis()
        val diff = now - chatRoom.lastMessageTime
        val oneDay = 24 * 60 * 60 * 1000
        
        when {
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
            diff < oneDay -> dateFormat.format(Date(chatRoom.lastMessageTime))
            diff < 2 * oneDay -> "Yesterday"
            else -> dayFormat.format(Date(chatRoom.lastMessageTime))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture with online indicator
            Box {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    tint = CBlue
                )
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (chatRoom.isActive) Color(0xFF4CAF50) else Color.Gray)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Chat Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        contactName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        timeString,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        chatRoom.lastMessage.ifBlank { "No messages yet" },
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD60A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                unreadCount.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
