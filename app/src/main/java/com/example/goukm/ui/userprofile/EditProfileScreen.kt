package com.example.goukm.ui.userprofile

import com.example.goukm.ui.theme.CBlue


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
import android.widget.Toast
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AccountCircle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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
    var isUploading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Image Picker
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    isUploading = true
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                    val url = UserProfileRepository.uploadProfilePicture(uid, it)
                    profilePictureUrl = url
                    Toast.makeText(navController.context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(navController.context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploading = false
                }
            }
        }
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
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(130.dp) // Slightly larger container for the badge
            ) {
                // Main Image Container
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(color = CBlue)
                    } else {
                        profilePictureUrl?.let { url ->
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } ?: run {
                            // Default Placeholder
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.AccountCircle,
                                contentDescription = "Default Profile",
                                modifier = Modifier.fillMaxSize(),
                                tint = Color.Gray
                            )
                        }
                    }
                }

                // Edit Icon Overlay (Bottom Right)
                if (!isUploading) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp) // Offset slightly inside
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CBlue)
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                            contentDescription = "Edit Picture",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
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
                    val updatedUser = user.copy(
                        name = name,
                        matricNumber = matricNumber,
                        profilePictureUrl = profilePictureUrl,
                        phoneNumber = phoneNumber,
                        email = email
                    )

                    // Update Firestore
                    scope.launch {
                        val success = UserProfileRepository.updateUserProfile(updatedUser)
                        if (success) {

                            onSave(updatedUser)
                            //navController.popBackStack()
                        }
                    }
                          },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CBlue)
            ) {
                Text("Save", color = Color.White)
            }
        }
    }
}
