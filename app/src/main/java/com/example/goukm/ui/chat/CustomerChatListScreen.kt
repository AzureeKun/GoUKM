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

val CBlue = Color(0xFF6B87C0)

data class ChatItem(
    val chatId: String,
    val contactName: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerChatListScreen(navController: NavHostController) {
    val dummyChats = remember {
        listOf(
            ChatItem("1", "Ahmad (Driver)", "See you in 5 minutes!", "2m ago", 2, true),
            ChatItem("2", "Fatimah (Driver)", "Thank you for riding!", "1h ago", 0, false),
            ChatItem("3", "Hassan (Driver)", "On my way to pickup", "3h ago", 1, true),
            ChatItem("4", "Aminah (Driver)", "Have a safe trip!", "Yesterday", 0, false)
        )
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
            NavigationBar(containerColor = CBlue) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("customer_dashboard") },
                    icon = { Icon(Icons.Default.Home, "Home", tint = Color.White) },
                    label = { Text("Home", color = Color.White) }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Message, "Chat", tint = Color.White) },
                    label = { Text("Chat", color = Color.White) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("customer_profile") },
                    icon = { Icon(Icons.Default.Person, "Profile", tint = Color.White) },
                    label = { Text("Profile", color = Color.White) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            if (dummyChats.isEmpty()) {
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
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(dummyChats) { chat ->
                        ChatListItem(
                            chat = chat,
                            onClick = {
                                navController.navigate("customer_chat/${chat.chatId}/${chat.contactName}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(chat: ChatItem, onClick: () -> Unit) {
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
                if (chat.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                            .align(Alignment.BottomEnd)
                    )
                }
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
                        chat.contactName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        chat.timestamp,
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
                        chat.lastMessage,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (chat.unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD60A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                chat.unreadCount.toString(),
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
