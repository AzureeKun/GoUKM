package com.example.goukm.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(message: Message, isMe: Boolean) {
    val dateFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val timeString = dateFormat.format(Date(message.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) CBlue else Color.White
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // For driver chat, show customer name if not me
                if (!isMe && message.senderRole != "driver") {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        color = CBlue,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                
                Text(
                    message.text,
                    fontSize = 15.sp,
                    color = if (isMe) Color.White else Color.Black
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        timeString,
                        fontSize = 11.sp,
                        color = if (isMe) Color(0xFFE0F7FA) else Color.Gray
                    )
                    if (isMe) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = if (message.isRead) "Read" else "Sent",
                            modifier = Modifier.size(16.dp),
                            tint = if (message.isRead) Color(0xFF4CAF50) else Color(0xFFB0BEC5)
                        )
                    }
                }
            }
        }
    }
}
