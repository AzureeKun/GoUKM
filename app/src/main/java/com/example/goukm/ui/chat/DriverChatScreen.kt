package com.example.goukm.ui.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverChatScreen(
    navController: NavHostController,
    chatId: String,
    contactName: String,
    phoneNumber: String = ""
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }
    val listState = rememberLazyListState()
    var isLoading by remember { mutableStateOf(true) }
    var userName by remember { mutableStateOf("Driver") }

    // Get current user's name
    LaunchedEffect(Unit) {
        val userDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId)
            .get()
        userDoc.addOnSuccessListener { doc ->
            userName = doc.getString("name") ?: "Driver"
        }
    }

    // Listen to messages in real-time
    LaunchedEffect(chatId) {
        ChatRepository.listenToMessages(chatId).collect { messageList ->
            messages.clear()
            messages.addAll(messageList)
            isLoading = false
        }
    }

    // Mark messages as read when screen opens or new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
             ChatRepository.markMessagesAsRead(chatId, isCustomer = false)
        }
    }

    // Scroll to bottom when new messages arrive
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
                            "Active",
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
                actions = {
                    // Call button
                    if (phoneNumber.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = "Call",
                                tint = Color.White
                            )
                        }
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
                .imePadding()
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CBlue)
                }
            } else {
                // Messages List
                val groupedMessages = remember(messages.toList()) {
                    messages.groupBy { formatHeaderDate(it.timestamp) }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    // verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    groupedMessages.forEach { (date, messagesInDate) ->
                        item(key = date) {
                            DateHeader(dateString = date)
                        }
                        items(messagesInDate, key = { it.id }) { message ->
                            Box(modifier = Modifier.padding(vertical = 6.dp)) {
                                MessageBubble(
                                    message = message,
                                    isMe = message.senderId == currentUserId
                                )
                            }
                        }
                    }
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
                                val text = messageText
                                messageText = ""
                                scope.launch {
                                    ChatRepository.sendMessage(chatId, text, userName, "driver")
                                }
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
