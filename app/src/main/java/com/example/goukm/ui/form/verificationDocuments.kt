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

// Enum to track which document
enum class DocumentType { DRIVING_LICENSE, VEHICLE_INSURANCE, BANK_QR }

// ---------------------------
// Single Document Composable
// ---------------------------
@Composable
fun verificationDocumentsScreen(
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

// ---------------------------
// Main Verification Composable (3 documents)
// ---------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun verificationDocuments(
    onUploadComplete: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // State for each document
    var drivingLicenseUri by remember { mutableStateOf<Uri?>(null) }
    var vehicleInsuranceUri by remember { mutableStateOf<Uri?>(null) }
    var bankQrUri by remember { mutableStateOf<Uri?>(null) }

    var capturingType by remember { mutableStateOf<DocumentType?>(null) }
    var capturedUri by remember { mutableStateOf<Uri?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            capturedUri?.let { uri ->
                when (capturingType) {
                    DocumentType.DRIVING_LICENSE -> drivingLicenseUri = uri
                    DocumentType.VEHICLE_INSURANCE -> vehicleInsuranceUri = uri
                    DocumentType.BANK_QR -> bankQrUri = uri
                    else -> {}
                }
                capturingType = null
                capturedUri = null
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && capturingType != null) {
            val uri = createDocumentFileUri(context)
            capturedUri = uri
            cameraLauncher.launch(uri)
        } else if (!granted) {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            when (capturingType) {
                DocumentType.DRIVING_LICENSE -> drivingLicenseUri = it
                DocumentType.VEHICLE_INSURANCE -> vehicleInsuranceUri = it
                DocumentType.BANK_QR -> bankQrUri = it
                else -> {}
            }
            capturingType = null
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
                Text("Upload or capture your documents", fontSize = 16.sp)

                // ------------------- Documents
                verificationDocumentsScreen(
                    label = "Driving License",
                    imageUri = drivingLicenseUri,
                    onCaptureClick = {
                        capturingType = DocumentType.DRIVING_LICENSE
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onPickClick = {
                        capturingType = DocumentType.DRIVING_LICENSE
                        galleryLauncher.launch("image/*")
                    },
                    onRemove = { drivingLicenseUri = null },
                    modifier = Modifier.fillMaxWidth()
                )

                verificationDocumentsScreen(
                    label = "Vehicle Insurance",
                    imageUri = vehicleInsuranceUri,
                    onCaptureClick = {
                        capturingType = DocumentType.VEHICLE_INSURANCE
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onPickClick = {
                        capturingType = DocumentType.VEHICLE_INSURANCE
                        galleryLauncher.launch("image/*")
                    },
                    onRemove = { vehicleInsuranceUri = null },
                    modifier = Modifier.fillMaxWidth()
                )

                verificationDocumentsScreen(
                    label = "Bank QR Code",
                    imageUri = bankQrUri,
                    onCaptureClick = {
                        capturingType = DocumentType.BANK_QR
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onPickClick = {
                        capturingType = DocumentType.BANK_QR
                        galleryLauncher.launch("image/*")
                    },
                    onRemove = { bankQrUri = null },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (drivingLicenseUri != null && vehicleInsuranceUri != null && bankQrUri != null) {
                            onUploadComplete?.invoke()
                        }
                    },
                    enabled = drivingLicenseUri != null && vehicleInsuranceUri != null && bankQrUri != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Upload for Verification")
                }
            }
        }
    )
}

// ---------------------------
// Helper: create URI for document capture
// ---------------------------
fun createDocumentFileUri(context: Context): Uri {
    val filename = "doc_capture_${System.currentTimeMillis()}.jpg"
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

// ---------------------------
// Preview
// ---------------------------
@Preview(showBackground = true)
@Composable
fun PreviewVerificationDocuments() {
    verificationDocuments()
}
