package com.example.goukm.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.goukm.ui.login.LoginScreen

@Composable
fun CustomerDashboard() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("CUSTOMER DASHBOARD", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        Button(onClick = { println("Book Ride Clicked!") }) {
            Text("Book a Ride")
        }
    }
}

/*@Preview(showBackground = true)
@Composable
fun CustDashboardPreview() {
    Scaffold { paddingValues ->
        CustomerDashboard(modifier = Modifier.padding(paddingValues))
    }
}*/