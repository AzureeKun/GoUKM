package com.example.goukm.ui.userprofile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    user: UserProfile,
    onSave: (UserProfile) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var matricNumber by remember { mutableStateOf(user.matricNumber) }
    var profilePictureUrl by remember { mutableStateOf(user.profilePictureUrl) }
    var email by remember { mutableStateOf(user.email) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber)}

    // Image Picker
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        profilePictureUrl = uri?.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Profile", color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CBlue))
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(CBlue)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                profilePictureUrl?.let { url ->
                    Image(
                        painter = rememberAsyncImagePainter(url),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Name Field
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(12.dp))

            // Matric Number Field
            OutlinedTextField(value = matricNumber, onValueChange = { matricNumber = it }, label = { Text("Matric Number") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(12.dp))

            // email Field
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(12.dp))

            // Phone Number Field
            OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    onSave(user.copy(name = name, matricNumber = matricNumber, profilePictureUrl = profilePictureUrl, phoneNumber = phoneNumber, email = email))
                    // kembali ke screen sebelum ini
                    navController?.popBackStack()
                          },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CBlue)
            ) {
                Text("Save", color = Color.White)
            }
        }
    }
}
