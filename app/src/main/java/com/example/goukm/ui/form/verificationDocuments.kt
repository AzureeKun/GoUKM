// File: VerificationDocumentsWithCameraX.kt
package com.example.goukm.ui.form

import com.example.goukm.ui.theme.CBlue

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.util.concurrent.Executor
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import kotlinx.coroutines.launch

enum class DocumentType { DRIVING_LICENSE, VEHICLE_GRANT, BANK_QR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun verificationDocuments(
    navController: androidx.navigation.NavHostController,
    onUploadComplete: (() -> Unit)? = null,
    viewModel: DriverApplicationViewModel
) {
    val context = LocalContext.current
    val executor = ContextCompat.getMainExecutor(context)
    val scope = rememberCoroutineScope()

    // Document URIs
    var drivingLicenseUri by remember { mutableStateOf<Uri?>(viewModel.drivingLicenseUri) }
    var vehicleGrantUri by remember { mutableStateOf<Uri?>(viewModel.vehicleGrantUri) }
    var bankQrUri by remember { mutableStateOf<Uri?>(viewModel.bankQrUri) }

    var showCameraOverlay by remember { mutableStateOf(false) }
    var capturingType by remember { mutableStateOf<DocumentType?>(null) }

    var showFileTooLargeDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && capturingType != null) showCameraOverlay = true
        else if (!granted) Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (isFileTooLarge(context, it)) { showFileTooLargeDialog = true; return@let }
            when (capturingType) {
                DocumentType.DRIVING_LICENSE -> drivingLicenseUri = it
                DocumentType.VEHICLE_GRANT -> vehicleGrantUri = it
                DocumentType.BANK_QR -> bankQrUri = it
                else -> {}
            }
            capturingType = null
        }
    }

    fun onCameraCaptured(uri: Uri) {
        if (isFileTooLarge(context, uri)) {
            try { File(uri.path!!).delete() } catch (_: Exception) {}
            showFileTooLargeDialog = true
            showCameraOverlay = false
            capturingType = null
            return
        }
        when (capturingType) {
            DocumentType.DRIVING_LICENSE -> drivingLicenseUri = uri
            DocumentType.VEHICLE_GRANT -> vehicleGrantUri = uri
            DocumentType.BANK_QR -> bankQrUri = uri
            else -> {}
        }
        capturingType = null
        showCameraOverlay = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Step 3 of 4: Upload Documents", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CBlue),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Upload or capture your documents")

            // ---------- Driving License (Landscape) ----------
            DocumentCard(
                label = "Driving License",
                imageUri = drivingLicenseUri,
                boxHeight = 140.dp,
               onCapture = {
                    capturingType = DocumentType.DRIVING_LICENSE
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onPick = {
                    capturingType = DocumentType.DRIVING_LICENSE
                    galleryLauncher.launch("image/*")
                },
                onRemove = { drivingLicenseUri = null }
            )

            // ---------- Vehicle Grant (Landscape) ----------
            DocumentCard(
                label = "Vehicle Grant",
                imageUri = vehicleGrantUri,
                boxHeight = 140.dp,
                onCapture = {
                    capturingType = DocumentType.VEHICLE_GRANT
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onPick = {
                    capturingType = DocumentType.VEHICLE_GRANT
                    galleryLauncher.launch("image/*")
                },
                onRemove = { vehicleGrantUri = null }
            )

            // ---------- Bank QR (Portrait) ----------
            DocumentCard(
                label = "Bank QR Code",
                imageUri = bankQrUri,
                boxHeight = 340.dp,
                onCapture = {
                    capturingType = DocumentType.BANK_QR
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onPick = {
                    capturingType = DocumentType.BANK_QR
                    galleryLauncher.launch("image/*")
                },
                onRemove = { bankQrUri = null }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (viewModel.lastError != null) {
                Text(viewModel.lastError!!, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
            }

            Button(
                onClick = {
                    if (drivingLicenseUri != null && vehicleGrantUri != null && bankQrUri != null) {
                        viewModel.setDocuments(drivingLicenseUri, vehicleGrantUri, bankQrUri)
                        scope.launch {
                            val success = viewModel.submitApplication(context)
                            if (success) {
                                onUploadComplete?.invoke()
                            }
                        }
                    }
                },
                enabled = drivingLicenseUri != null && vehicleGrantUri != null && bankQrUri != null && !viewModel.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (viewModel.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Submit Application")
                }
            }
        }

        // Camera overlay
        if (showCameraOverlay) {
            CameraOverlay(
                onCaptured = { uri -> onCameraCaptured(uri) },
                onCancel = { showCameraOverlay = false; capturingType = null },
                executor = executor
            )
        }

        // File too large dialog
        if (showFileTooLargeDialog) {
            AlertDialog(
                onDismissRequest = { showFileTooLargeDialog = false },
                title = { Text("File Too Large") },
                text = { Text("Please use an image smaller than 5MB.") },
                confirmButton = { TextButton(onClick = { showFileTooLargeDialog = false }) { Text("OK") } }
            )
        }
    }
}

@Composable
fun DocumentCard(
    label: String,
    imageUri: Uri?,
    boxHeight: Dp = 140.dp, // landscape height
    onCapture: () -> Unit,
    onPick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(boxHeight + 80.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally // Center all children
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)

            // Image box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(boxHeight)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = label,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Text("No image")
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCapture) { Text("Capture") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onPick) { Text("Pick") }
                if (imageUri != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onRemove) { Text("Remove") }
                }
            }
        }
    }
}

