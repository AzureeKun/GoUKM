package com.example.goukm.ui.form

import com.example.goukm.ui.theme.CBlue

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.io.InputStream
import java.util.concurrent.Executor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

enum class VerificationSide { FRONT, BACK }

// ---------- Helpers ----------
fun isFileTooLarge(context: Context, uri: Uri, maxMB: Int = 5): Boolean {
    val afd = context.contentResolver.openAssetFileDescriptor(uri, "r") ?: return false
    val size = afd.length
    afd.close()
    val maxBytes = maxMB * 1024L * 1024L
    return size > maxBytes
}

fun isLandscapeImage(context: Context, uri: Uri): Boolean {
    var input: InputStream? = null
    return try {
        input = context.contentResolver.openInputStream(uri)
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(input, null, opts)
        opts.outWidth > opts.outHeight
    } catch (e: Exception) {
        false
    } finally {
        input?.close()
    }
}

// ---------- Camera Overlay ----------
@Composable
fun CameraOverlay(
    onCaptured: (Uri) -> Unit,
    onCancel: () -> Unit,
    executor: Executor
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val preview = Preview.Builder().build()
                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                imageCapture = capture

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                    try {
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 340.dp, height = 220.dp)
                .border(3.dp, Color.White, RoundedCornerShape(8.dp))
        )

        Text(
            text = "Align inside the box",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = Color.White)
            }

            Button(onClick = {
                val capture = imageCapture ?: return@Button
                val file = File(context.cacheDir, "ic_${System.currentTimeMillis()}.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                capture.takePicture(
                    outputOptions,
                    executor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exception: ImageCaptureException) {
                            exception.printStackTrace()
                            Toast.makeText(context, "Capture failed", Toast.LENGTH_SHORT).show()
                        }

                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            onCaptured(Uri.fromFile(file))
                        }
                    }
                )
            }) {
                Text("Capture")
            }
        }
    }
}

// ---------- Main Verification IC ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun verificationIC(
    navController: androidx.navigation.NavHostController,
    onUploadComplete: (() -> Unit)? = null,
    viewModel: DriverApplicationViewModel
) {
    val context = LocalContext.current
    val executor = ContextCompat.getMainExecutor(context)

    var frontUri by remember { mutableStateOf<Uri?>(viewModel.icFrontUri) }
    var backUri by remember { mutableStateOf<Uri?>(viewModel.icBackUri) }

    var showCameraOverlay by remember { mutableStateOf(false) }
    var cameraForSide by remember { mutableStateOf<VerificationSide?>(null) }

    var showFileTooLargeDialog by remember { mutableStateOf(false) }
    var showOrientationDialog by remember { mutableStateOf(false) }

    val applicationStatus by remember { mutableStateOf("draft") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && cameraForSide != null) {
            showCameraOverlay = true
        } else if (!granted) {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (isFileTooLarge(context, it)) {
                showFileTooLargeDialog = true
            }
            if (!isLandscapeImage(context, it)) {
                showOrientationDialog = true
            }
            when (cameraForSide) {
                VerificationSide.FRONT -> frontUri = it
                VerificationSide.BACK -> backUri = it
                else -> {}
            }
            cameraForSide = null
        }
    }

    fun onCameraCaptured(uri: Uri) {
        if (isFileTooLarge(context, uri)) {
            try { File(uri.path!!).delete() } catch (_: Exception) {}
            showFileTooLargeDialog = true
            showCameraOverlay = false
            cameraForSide = null
            return
        }
        if (!isLandscapeImage(context, uri)) {
            try { File(uri.path!!).delete() } catch (_: Exception) {}
            showOrientationDialog = true
            showCameraOverlay = false
            cameraForSide = null
            return
        }
        when (cameraForSide) {
            VerificationSide.FRONT -> frontUri = uri
            VerificationSide.BACK -> backUri = uri
            else -> {}
        }
        cameraForSide = null
        showCameraOverlay = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Step 2 of 4: Upload Identity Card For Verification", color = Color.White) },
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
        Box(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Upload or capture your IC (front & back)")

                // FRONT IC
                Card(modifier = Modifier.fillMaxWidth().height(220.dp), shape = RoundedCornerShape(12.dp)) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally // <-- center everything
                    ) {
                        Text("Front", style = MaterialTheme.typography.titleMedium)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            frontUri?.let {
                                androidx.compose.foundation.Image(
                                    painter = rememberAsyncImagePainter(it),
                                    contentDescription = "Front IC",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } ?: Text("No image")
                        }
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                cameraForSide = VerificationSide.FRONT
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }) { Text("Capture") }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = {
                                cameraForSide = VerificationSide.FRONT
                                galleryLauncher.launch("image/*")
                            }) { Text("Pick") }
                            if (frontUri != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = { frontUri = null }) { Text("Remove") }
                            }
                        }
                    }
                }

// BACK IC
                Card(modifier = Modifier.fillMaxWidth().height(220.dp), shape = RoundedCornerShape(12.dp)) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally // <-- center everything
                    ) {
                        Text("Back", style = MaterialTheme.typography.titleMedium)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            backUri?.let {
                                androidx.compose.foundation.Image(
                                    painter = rememberAsyncImagePainter(it),
                                    contentDescription = "Back IC",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } ?: Text("No image")
                        }
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                cameraForSide = VerificationSide.BACK
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }) { Text("Capture") }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = {
                                cameraForSide = VerificationSide.BACK
                                galleryLauncher.launch("image/*")
                            }) { Text("Pick") }
                            if (backUri != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = { backUri = null }) { Text("Remove") }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { 
                        if (frontUri != null && backUri != null) {
                            viewModel.setIc(frontUri, backUri)
                            onUploadComplete?.invoke()
                        }
                    },
                    enabled = frontUri != null && backUri != null && applicationStatus != "under_review",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (applicationStatus == "under_review") "Application Pending" else "Next")
                }
            }

            // Camera overlay
            if (showCameraOverlay) {
                CameraOverlay(
                    onCaptured = { uri -> onCameraCaptured(uri) },
                    onCancel = { showCameraOverlay = false; cameraForSide = null },
                    executor = executor
                )
            }

            // Dialogs
            if (showFileTooLargeDialog) {
                AlertDialog(
                    onDismissRequest = { showFileTooLargeDialog = false },
                    title = { Text("File Too Large") },
                    text = { Text("Please use an image smaller than 5MB.") },
                    confirmButton = { TextButton(onClick = { showFileTooLargeDialog = false }) { Text("OK") } }
                )
            }
            if (showOrientationDialog) {
                AlertDialog(
                    onDismissRequest = { showOrientationDialog = false },
                    title = { Text("Wrong Orientation") },
                    text = { Text("Please take the IC in landscape orientation.") },
                    confirmButton = { TextButton(onClick = { showOrientationDialog = false }) { Text("OK") } }
                )
            }
        }
    }
}
