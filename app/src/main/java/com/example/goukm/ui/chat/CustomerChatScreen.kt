package com.example.goukm.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

data class ChatMessage(
    val messageId: String,
    val text: String,
    val timestamp: String,
    val isSentByMe: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerChatScreen(
    navController: NavHostController,
    chatId: String,
    contactName: String
) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage("1", "Hi! I've arrived at the pickup point.", "10:30 AM", false),
            ChatMessage("2", "Great! I'm coming down now", "10:31 AM", true),
            ChatMessage("3", "See you in 5 minutes!", "10:32 AM", false),
            ChatMessage("4", "Okay, thanks!", "10:32 AM", true)
        )
    }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            contactName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Online",
                            color = Color(0xFFE0F7FA),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CBlue)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFECEFF1))
                .padding(paddingValues)
        ) {
            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message)
                }
            }

            // Message Input
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...", color = Color.Gray) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CBlue,
                            unfocusedBorderColor = Color.LightGray,
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5)
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                messages.add(
                                    ChatMessage(
                                        messageId = (messages.size + 1).toString(),
                                        text = messageText,
                                        timestamp = "Now",
                                        isSentByMe = true
                                    )
                                )
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(CBlue)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isSentByMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isSentByMe) 16.dp else 4.dp,
                bottomEnd = if (message.isSentByMe) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isSentByMe) CBlue else Color.White
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    message.text,
                    fontSize = 15.sp,
                    color = if (message.isSentByMe) Color.White else Color.Black
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    message.timestamp,
                    fontSize = 11.sp,
                    color = if (message.isSentByMe) Color(0xFFE0F7FA) else Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
