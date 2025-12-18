package com.example.goukm.ui.userprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.goukm.navigation.NavRoutes
import com.example.goukm.ui.register.AuthViewModel
import com.example.goukm.ui.dashboard.BottomNavigationBarDriver
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfileScreen(
    navController: NavHostController,
    user: UserProfile?,
    authViewModel: AuthViewModel,
    onEditProfile: (UserProfile) -> Unit,
    onLogout: () -> Unit,
    selectedNavIndex: Int // Pass logic index for bottom bar navigation highlight
) {
    val scope = rememberCoroutineScope()
    // We already have user data passed in, which includes vehicle info now.

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
                title = { Text("Driver Profile", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CBlue)
            )
        },
        bottomBar = {
            // Reusing the Driver Bottom Bar, but assuming we can handle navigation back to dashboard
            BottomNavigationBarDriver(
                selectedIndex = selectedNavIndex, // 3 is usually Profile if we added it, but let's assume valid index is passed
                onSelected = { index ->
                    when (index) {
                        0 -> navController.navigate(NavRoutes.DriverDashboard.route) {
                            popUpTo(NavRoutes.DriverDashboard.route) { inclusive = true }
                        }
                        1 -> navController.navigate(NavRoutes.DriverScore.route)
                        2 -> navController.navigate(NavRoutes.DriverEarning.route)
                        3 -> { /* Already here */ }
                    }
                }
            )
        }
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
                            contentDescription = "Default Picture",
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
                        Text("Driver Account", fontSize = 14.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                Button(
                    onClick = {
                        scope.launch {
                            authViewModel.switchActiveRole("customer")
                            navController.navigate("customer_dashboard") {
                                popUpTo("driver_dashboard") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CBlue)
                ) {
                    Text(
                        text = "Switch to Customer Mode",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                Button(
                    onClick = { onEditProfile(user) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CBlue)
                ) {
                    Text("Edit Profile", color = Color.White)
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            // Vehicle Details Card
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CBlue)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Vehicle Details", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Spacer(Modifier.height(12.dp))
                        ReadOnlyField(label = "Vehicle Type", value = user.vehicleType.ifEmpty { "Not specified" })
                        Spacer(Modifier.height(12.dp))
                        ReadOnlyField(label = "Plate Number", value = user.vehiclePlateNumber.ifEmpty { "Not specified" })
                        Spacer(Modifier.height(12.dp))
                        ReadOnlyField(label = "License Number", value = user.licenseNumber.ifEmpty { "Not specified" })
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

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
