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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.goukm.R
import com.example.goukm.navigation.NavRoutes

val CBlue = Color(0xFF6b87c0)
val CRed = Color(0xFFE53935)
val CanvaSansBold = FontFamily.Default
val PoppinsLight = FontFamily.Default
val PoppinsSemiBold = FontFamily(
    Font(R.font.poppins_semibold)
)

data class UserProfile(
    val name: String,
    val matricNumber: String,
    val profilePictureUrl: String? = null
)

@Composable
fun ReadOnlyField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {

        // Label
        Text(
            text = label,
            color = Color.Black.copy(alpha = 0.7f),
            fontSize = 14.sp,
            style = TextStyle(fontFamily = PoppinsLight)
        )

        Spacer(Modifier.height(4.dp))

        // Fake TextField box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, MaterialTheme.shapes.small)
                .border(1.dp, Color.Black, MaterialTheme.shapes.small)
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Text(
                text = value,
                color = Color.Black,
                fontSize = 16.sp,
                style = TextStyle(fontFamily = PoppinsLight)
            )
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    NavigationBar(
        containerColor = CBlue
    ) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(NavRoutes.CustomerDashboard.route) },
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
            selected = true, // current screen
            onClick = { /* Already on Profile */ },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(user: UserProfile, navController: NavHostController, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                title = {
                    Text(
                        "My Profile",
                        color = Color.White,
                        fontSize = 20.sp,
                        style = TextStyle(
                            fontFamily = PoppinsSemiBold,
                            lineHeight = 21.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CBlue
                )
            )
        },
        bottomBar = { BottomBar(navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Filled.AccountCircle),
                            contentDescription = "Profile Picture",
                            tint = Color.White,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(CBlue)
                                .padding(8.dp)
                        )

                        Spacer(Modifier.width(20.dp))

                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = user.name,
                                color = Color.Black,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CanvaSansBold
                            )

                            Text(
                                text = "@${user.matricNumber}",
                                color = Color.Black.copy(alpha = 0.8f),
                                fontSize = 16.sp,
                                fontFamily = PoppinsLight
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(40.dp)) }

            // Bio Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CBlue),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Bio Information",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )

                        Spacer(Modifier.height(12.dp))

                        ReadOnlyField(label = "Name", value = user.name)
                        Spacer(Modifier.height(12.dp))

                        ReadOnlyField(label = "Matric Number", value = "@${user.matricNumber}")
                        Spacer(Modifier.height(12.dp))

                        ReadOnlyField(label = "Gender", value = "Male")
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            // Contact Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CBlue),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Contact Information",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )

                        Spacer(Modifier.height(12.dp))

                        ReadOnlyField(label = "Email", value = "student@siswa.ukm.edu.my")
                        Spacer(Modifier.height(12.dp))

                        ReadOnlyField(label = "Phone Number", value = "012-3456789")
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CBlue),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column (
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Start Working",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Make side income by becoming our driver!",
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { /* TODO: Start Working */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Apply Here",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                Button(
                    onClick = { /* TODO: Handle logout */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Logout",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    val dummyUser = UserProfile(
        name = "Ahmad Bin Abu",
        matricNumber = "A18CS0123"
    )
    CustomerProfileScreen(
        user = dummyUser,
        navController = rememberNavController()
    )
}