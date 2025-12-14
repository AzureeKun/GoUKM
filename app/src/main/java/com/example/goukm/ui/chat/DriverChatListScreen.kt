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
import com.example.goukm.ui.dashboard.BottomNavigationBarDriver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverChatListScreen(navController: NavHostController) {
    var selectedNavIndex by remember { mutableStateOf(1) } // Chat is index 1 (Score button acts as chat)
    
    val dummyChats = remember {
        listOf(
            ChatItem("1", "Siti (Customer)", "Thank you, driver!", "5m ago", 1, false),
            ChatItem("2", "Ali (Customer)", "I'm waiting at the gate", "30m ago", 0, true),
            ChatItem("3", "Nur (Customer)", "Can you wait 2 minutes?", "2h ago", 3, false),
            ChatItem("4", "Ahmad (Customer)", "Thanks for the ride!", "Yesterday", 0, false)
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
            BottomNavigationBarDriver(
                selectedIndex = selectedNavIndex,
                onSelected = { index ->
                    selectedNavIndex = index
                    when (index) {
                        0 -> navController.navigate("driver_dashboard") { 
                            popUpTo("driver_dashboard") { inclusive = true }
                        }
                        2 -> navController.navigate("driver_earning")
                        3 -> navController.navigate("driver_profile")
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
                                navController.navigate("driver_chat/${chat.chatId}/${chat.contactName}")
                            }
                        )
                    }
                }
            }
        }
    }
}
