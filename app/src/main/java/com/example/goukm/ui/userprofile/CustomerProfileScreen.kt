package com.example.goukm.ui.userprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.layout.ContentScale

val CBlue = Color(0xFF6b87c0)

data class UserProfile(
    val name: String,
    val matricNumber: String,
    val profilePictureUrl: String? = null,
    val email: String,
    val phoneNumber: String
)

@Composable
fun ReadOnlyField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, color = Color.Black.copy(alpha = 0.7f), fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, MaterialTheme.shapes.small)
                .border(1.dp, Color.Black, MaterialTheme.shapes.small)
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Text(text = value, color = Color.Black, fontSize = 16.sp)
        }
    }
}

@Composable
fun BottomBarCust(navController: NavHostController) {
    NavigationBar(containerColor = CBlue) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("customer_dashboard") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White) },
            label = { Text("Home", color = Color.White) },
            alwaysShowLabel = true
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* TODO: Navigate Chat */ },
            icon = { Icon(Icons.Default.Message, contentDescription = "Chat", tint = Color.White) },
            label = { Text("Chat", color = Color.White) },
            alwaysShowLabel = true
        )
        NavigationBarItem(
            selected = true,
            onClick = { /* Already on Profile */ },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White) },
            label = { Text("Profile", color = Color.White) },
            alwaysShowLabel = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    navController: NavHostController,
    user: UserProfile?,
    onEditProfile: (UserProfile) -> Unit,
    onLogout: () -> Unit

) {
    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CBlue)
            )
        },
        bottomBar = { BottomBarCust(navController) }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    // Image
                    user.profilePictureUrl?.let { url ->
                        Image(
                            painter = rememberAsyncImagePainter(url),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } ?: run {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(CBlue)
                                .padding(8.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text(user.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("@${user.matricNumber}", fontSize = 16.sp, color = Color.Black.copy(alpha = 0.7f))
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                Button(
                    onClick = { onEditProfile(user!!) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CBlue)
                ) {
                    Text("Edit Profile", color = Color.White)
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            // ---------- All Other Cards Remain ----------

            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CBlue)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Contact Information", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Spacer(Modifier.height(12.dp))
                        ReadOnlyField(label = "Email", value = user.email)
                        Spacer(Modifier.height(12.dp))
                        ReadOnlyField(label = "Phone Number", value = user.phoneNumber)
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CBlue)) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Start Working", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Spacer(Modifier.height(8.dp))
                        Text("Make side income by becoming our driver!", fontSize = 12.sp, color = Color.Black)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate("driver_application") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Apply Here", color = Color.Black)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Logout", color = Color.White)
                }
            }
        }
    }
}
