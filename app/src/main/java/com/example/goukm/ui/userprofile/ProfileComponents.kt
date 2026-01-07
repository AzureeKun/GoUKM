package com.example.goukm.ui.userprofile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(title, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B), fontSize = 16.sp)
            Spacer(Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column {
        Text(label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(value, color = Color(0xFF1E293B), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}
