package com.example.goukm.ui.userprofile

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.example.goukm.navigation.NavRoutes
import com.example.goukm.ui.theme.CBlue
import com.example.goukm.ui.register.AuthViewModel
import com.example.goukm.util.DriverEligibilityChecker
import kotlinx.coroutines.launch



data class Vehicle(
    val id: String = "",
    val brand: String = "",
    val color: String = "",
    val plateNumber: String = "",
    val licenseNumber: String = "",
    val grantUrl: String = "",
    val status: String = "Approved", // Approved, Pending, Rejected
    val lastEditedAt: Long = 0
)

data class UserProfile(
    val uid: String = "",
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
    val onlineDays: List<String> = emptyList(),
    val onlineWorkDurations: Map<String, Long> = emptyMap(), // date -> minutes
    val vehicles: List<Vehicle> = emptyList(),
    val preferredPaymentMethod: String = "CASH"
)

@Composable
fun ReadOnlyField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
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
    val surfaceColor = Color(0xFFF5F7FB)
    val darkNavy = Color(0xFF1E293B)

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CBlue)
        }
        return
    }

    val applicationStatus by authViewModel.driverApplicationStatus.collectAsState()

    Scaffold(
        containerColor = surfaceColor
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header Section
            item {
                Spacer(Modifier.height(48.dp))
                Box(contentAlignment = Alignment.Center) {
                    // Profile Image with soft glow
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier.size(120.dp)
                    ) {
                        user.profilePictureUrl?.let { url ->
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } ?: run {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "Default Picture",
                                modifier = Modifier.padding(24.dp),
                                tint = CBlue.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = user.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkNavy
                )
                Text(
                    text = "@${user.matricNumber}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(Modifier.height(12.dp))
                
                // Account Type Pill
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Customer Account",
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                
                Spacer(Modifier.height(32.dp))
            }

            // Contact Info Card
            item {
                ProfileCard(title = "Contact Info") {
                    InfoRow(label = "Email", value = user.email)
                    Spacer(Modifier.height(16.dp))
                    InfoRow(label = "Phone", value = user.phoneNumber)
                }
            }

            // Role Switching / Application Section
            item {
                Spacer(Modifier.height(16.dp))
                ProfileCard(title = "Account Actions") {
                    if (user.role_driver) {
                        Button(
                            onClick = {
                                scope.launch {
                                    authViewModel.switchActiveRole("driver")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CBlue)
                        ) {
                            Text("Switch to Driver Mode", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        val eligibilityResult = DriverEligibilityChecker.checkEligibility(user)
                        val isPending = applicationStatus == "under_review"

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (isPending) "Application Pending" else "Join as Driver",
                                fontWeight = FontWeight.Bold,
                                color = darkNavy
                            )
                            Text(
                                if (isPending) "Your application is under review" else "Earn by driving your peers",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (isPending) navController.navigate("driver_application_status")
                                    else if (eligibilityResult.isEligible) navController.navigate("driver_application")
                                    else Toast.makeText(context, eligibilityResult.reason, Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPending) Color(0xFFFFA000) else CBlue
                                )
                            ) {
                                Text(
                                    if (isPending) "Check Status" else "Register as Driver",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { onEditProfile(user) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                    ) {
                        Text("Edit Profile Details", color = CBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Logout Section
            item {
                Spacer(Modifier.height(24.dp))
                TextButton(
                    onClick = onLogout,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Text("Logout from GoUKM", color = Color.Red.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

