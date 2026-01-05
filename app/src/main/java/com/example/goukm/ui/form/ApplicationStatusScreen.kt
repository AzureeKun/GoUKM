package com.example.goukm.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.goukm.ui.theme.CBlue

@Composable
fun DriverApplicationStatusScreen(
    status: String?,
    onResubmit: () -> Unit,
    onBackToDashboard: () -> Unit
) {
    val title = when (status) {
        "under_review" -> "Application Under Review"
        "rejected" -> "Application Rejected"
        else -> "Application Status"
    }
    
    val message = when (status) {
        "under_review" -> "We have received your documents. Our team is currently reviewing your profile. You will be notified automatically once it's approved."
        "rejected" -> "Unfortunately, your application was not approved. This could be due to blurry documents or incorrect information."
        else -> "Please submit your documents to start your journey as a driver."
    }

    val icon = if (status == "rejected") Icons.Default.Warning else Icons.Default.HourglassEmpty
    val iconColor = if (status == "rejected") Color(0xFFE53935) else CBlue

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = iconColor
        )
        
        Spacer(Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onBackToDashboard,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Back to Profile", color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onResubmit,
            enabled = status == "rejected",
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                if (status == "rejected") "Resubmit Documents" else "Under Review",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
