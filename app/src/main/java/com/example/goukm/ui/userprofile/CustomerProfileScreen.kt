package com.example.goukm.ui.userprofile

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.example.goukm.ui.theme.CBlue
import com.example.goukm.ui.register.AuthViewModel
import com.example.goukm.util.DriverEligibilityChecker
import kotlinx.coroutines.launch



data class UserProfile(
    val name: String = "",
    val matricNumber: String = "",
    val profilePictureUrl: String? = null,
    val email: String = "",
    val phoneNumber: String = "",
    val role_customer: Boolean = true,
    val role_driver: Boolean = false,
    val licenseNumber: String = "",
    val vehiclePlateNumber: String = "",
    val vehicleType: String = "",
    val carBrand: String = "",
    val carColor: String = "",
    // Academic information from SMPWeb
    val faculty: String = "",
    val academicProgram: String = "",
    val yearOfStudy: Int = 0,
    val enrolmentLevel: String = "",
    val academicStatus: String = "",
    val batch: String = "",
    val isAvailable: Boolean = false,
    val onlineDays: List<String> = emptyList()
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
    authViewModel: AuthViewModel,
    onEditProfile: (UserProfile) -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val activeRole by authViewModel.activeRole.collectAsState()
    val isDriver = activeRole == "driver"
    val context = LocalContext.current

    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val applicationStatus by authViewModel.driverApplicationStatus.collectAsState()

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
                        Text("Customer Account", fontSize = 14.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                // Only show switch-to-driver when this account is approved as driver
                if (user.role_driver) {
                    Button(
                        onClick = {
                            scope.launch {
                                authViewModel.switchActiveRole("driver") // suspend call
                                navController.navigate("driver_dashboard") {
                                    popUpTo("customer_dashboard") { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text(
                            text = "Switch to Driver Mode",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                // Show "Start Working" only if user is not currently a driver AND hasn't already applied
                if (!isDriver && !user.role_driver) {
                    val eligibilityResult = DriverEligibilityChecker.checkEligibility(user)
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CBlue)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (applicationStatus == "under_review") "Application Pending" else "Start Working",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                if (applicationStatus == "under_review") "Your application is currently being reviewed. Please wait for approval."
                                else "Make side income by becoming our driver!",
                                fontSize = 12.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )

                            if (applicationStatus == "under_review") {
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { navController.navigate("driver_application_status") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text("Check Status", color = Color.Black)
                                }
                            } else {
                                // Show eligibility status
                                if (!eligibilityResult.isEligible) {
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        eligibilityResult.reason,
                                        fontSize = 12.sp,
                                        color = Color.Red,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (eligibilityResult.isEligible) {
                                            navController.navigate("driver_application")
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "You must be in Year 2 and above to become a driver.",
                                                Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text(if (applicationStatus == "rejected") "Re-apply Now" else "Apply Here", color = Color.Black)
                                }
                            }
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
