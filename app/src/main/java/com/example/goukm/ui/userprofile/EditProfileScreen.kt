@file:OptIn(ExperimentalMaterial3Api::class)
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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
    var phoneNumber by remember { mutableStateOf(user.phoneNumber) }
    var isUploading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val surfaceColor = Color(0xFFF5F7FB)
    val darkNavy = Color(0xFF1E293B)

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
        containerColor = surfaceColor,
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = darkNavy) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back", tint = darkNavy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // Profile Picture with Edit Badge
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(140.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isUploading) {
                            CircularProgressIndicator(color = CBlue)
                        } else {
                            profilePictureUrl?.let { url ->
                                Image(
                                    painter = rememberAsyncImagePainter(url),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize().clickable { launcher.launch("image/*") },
                                    contentScale = ContentScale.Crop
                                )
                            } ?: run {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.AccountCircle,
                                    contentDescription = "Default Profile",
                                    modifier = Modifier.size(80.dp).clickable { launcher.launch("image/*") },
                                    tint = CBlue.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
                
                if (!isUploading) {
                    FilledIconButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = CBlue)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                            contentDescription = "Edit Picture",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // Input Fields Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Personal Details", fontWeight = FontWeight.ExtraBold, color = darkNavy, fontSize = 16.sp)
                    Spacer(Modifier.height(24.dp))
                    
                    CozyTextField(value = name, onValueChange = { name = it }, label = "Full Name")
                    Spacer(Modifier.height(20.dp))
                    CozyTextField(value = matricNumber, onValueChange = { matricNumber = it }, label = "Matric Number")
                    Spacer(Modifier.height(20.dp))
                    CozyTextField(value = email, onValueChange = { email = it }, label = "Email Address")
                    Spacer(Modifier.height(20.dp))
                    CozyTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = "Phone Number")
                }
            }

            Spacer(Modifier.height(40.dp))

            // Save Button
            Button(
                onClick = {
                    val updatedUser = user.copy(
                        name = name.trim(),
                        matricNumber = matricNumber.trim(),
                        profilePictureUrl = profilePictureUrl,
                        phoneNumber = phoneNumber.trim(),
                        email = email.trim()
                    )
                    scope.launch {
                        val success = UserProfileRepository.updateUserProfile(updatedUser)
                        if (success) {
                            onSave(updatedUser)
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CBlue)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun CozyTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = CBlue,
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            focusedLabelColor = CBlue
        ),
        singleLine = true
    )
}
