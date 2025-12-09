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

@Composable
fun DriverApplicationStatusScreen(
    status: String?,
    onResubmit: () -> Unit,
    onBackToDashboard: () -> Unit
) {
    val message = when (status) {
        "under_review" -> "We have received your documents. Our team will review them within 24 hours. You will get access automatically if approved."
        "rejected" -> "Your previous application was rejected. Please review your documents and resubmit."
        else -> "Submit your documents to start your driver application."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Driver Application",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = onBackToDashboard
        ) {
            Text("Back")
        }

        Button(
            onClick = onResubmit,
            enabled = status == "rejected"
        ) {
            Text(if (status == "rejected") "Resubmit Application" else "Pending Review")
        }
    }
}
