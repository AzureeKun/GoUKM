package com.example.goukm.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.goukm.navigation.NavRoutes
import com.example.goukm.ui.userprofile.CBlue

@Composable
fun BottomBar(navController: NavHostController) {
    NavigationBar(
        containerColor = CBlue
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { /* TODO: Navigate Home */ },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color.White
                )
            },
            label = { Text("Home", color = Color.White) },
            alwaysShowLabel = true
        )

        NavigationBarItem(
            selected = false,
            onClick = { /* TODO: Navigate Bubble */ },
            icon = {
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = "Chat",
                    tint = Color.White
                )
            },
            label = { Text("Chat", color = Color.White) },
            alwaysShowLabel = true
        )

        NavigationBarItem(
            selected = false, // current screen
            onClick = { navController.navigate(NavRoutes.CustomerProfile.route) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White
                )
            },
            label = { Text("Profile", color = Color.White) },
            alwaysShowLabel = true
        )
    }
}

@Composable
fun CustomerDashboard(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE3F2FD))
                .padding(paddingValues),
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
}

/*@Preview(showBackground = true)
@Composable
fun CustDashboardPreview() {
    Scaffold { paddingValues ->
        CustomerDashboard(modifier = Modifier.padding(paddingValues))
    }
}*/