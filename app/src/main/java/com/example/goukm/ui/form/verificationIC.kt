package com.example.goukm.ui.form

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.goukm.ui.register.AuthViewModel
import com.example.goukm.ui.register.AuthViewModelFactory

enum class VerificationSide { FRONT, BACK }

@Composable
fun verificationICScreen(
    label: String,
    imageUri: Uri?,
    onCaptureClick: () -> Unit,
    onPickClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(220.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "$label image",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Text(
                        "No image",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    TextButton(onClick = onCaptureClick) { Text("Capture") }
                    TextButton(onClick = onPickClick) { Text("Pick") }
                }
                if (imageUri != null) {
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove $label",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun verificationIC(
    onUploadComplete: (() -> Unit)? = null,
    applicationViewModel: DriverApplicationViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current

    var frontUri by remember { mutableStateOf<Uri?>(applicationViewModel.icFrontUri) }
    var backUri by remember { mutableStateOf<Uri?>(applicationViewModel.icBackUri) }
    var capturingSide by remember { mutableStateOf<VerificationSide?>(null) }
    var capturedUri by remember { mutableStateOf<Uri?>(null) }
    val applicationStatus by authViewModel.driverApplicationStatus.collectAsState()

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            capturedUri?.let { uri ->
                when (capturingSide) {
                    VerificationSide.FRONT -> {
                        frontUri = uri
                        applicationViewModel.setIc(front = uri, back = backUri)
                    }
                    VerificationSide.BACK -> {
                        backUri = uri
                        applicationViewModel.setIc(front = frontUri, back = uri)
                    }
                    else -> {}
                }
                capturingSide = null
                capturedUri = null
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && capturingSide != null) {
            val uri = createImageFileUri(context)
            capturedUri = uri
            cameraLauncher.launch(uri)
        } else if (!granted) {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            when (capturingSide) {
                VerificationSide.FRONT -> {
                    frontUri = it
                    applicationViewModel.setIc(front = it, back = backUri)
                }
                VerificationSide.BACK -> {
                    backUri = it
                    applicationViewModel.setIc(front = frontUri, back = it)
                }
                else -> {}
            }
            capturingSide = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verification", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CBlue)
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Upload or capture your IC (front & back)", fontSize = 16.sp)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // FRONT
                    verificationICScreen(
                        label = "Front",
                        imageUri = frontUri,
                        onCaptureClick = {
                            capturingSide = VerificationSide.FRONT
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        onPickClick = {
                            capturingSide = VerificationSide.FRONT
                            galleryLauncher.launch("image/*")
                        },
                        onRemove = {
                            frontUri = null
                            applicationViewModel.setIc(front = null, back = backUri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // BACK
                    verificationICScreen(
                        label = "Back",
                        imageUri = backUri,
                        onCaptureClick = {
                            capturingSide = VerificationSide.BACK
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        onPickClick = {
                            capturingSide = VerificationSide.BACK
                            galleryLauncher.launch("image/*")
                        },
                        onRemove = {
                            backUri = null
                            applicationViewModel.setIc(front = frontUri, back = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (frontUri != null && backUri != null) {
                            onUploadComplete?.invoke()
                        }
                    },
                    enabled = frontUri != null && backUri != null && applicationStatus != "under_review",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (applicationStatus == "under_review") "Application Pending"
                        else "Next"
                    )
                }
            }
        }
    )
}

// Helper: create URI for camera capture
fun createImageFileUri(context: Context): Uri {
    val filename = "ic_capture_${System.currentTimeMillis()}.jpg"
    val resolver = context.contentResolver

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Verification")
        }
    }

    return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        ?: throw IllegalStateException("Unable to create media store entry")
}

// Preview
@Preview(showBackground = true)
@Composable
fun PreviewVerificationIC() {
    verificationIC()
}