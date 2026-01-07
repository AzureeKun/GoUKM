package com.example.goukm.ui.userprofile

import com.example.goukm.ui.theme.CBlue

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.goukm.navigation.NavRoutes
import com.example.goukm.ui.register.AuthViewModel
import com.example.goukm.ui.dashboard.BottomNavigationBarDriver
import kotlinx.coroutines.launch
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.foundation.clickable
import com.example.goukm.ui.userprofile.Vehicle
import com.example.goukm.ui.userprofile.UserProfileRepository
import java.util.UUID
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.platform.LocalContext
import com.example.goukm.ui.form.DocumentCard
import com.example.goukm.ui.form.DocumentType
import com.example.goukm.ui.form.CameraOverlay
import com.example.goukm.ui.form.isFileTooLarge
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import androidx.core.content.ContextCompat
import java.io.File
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfileScreen(
    navController: NavHostController,
    user: UserProfile?,
    authViewModel: AuthViewModel,
    onEditProfile: (UserProfile) -> Unit,
    onLogout: () -> Unit,
    selectedNavIndex: Int // Pass logic index for bottom bar navigation highlight
) {
    val scope = rememberCoroutineScope()
    // We already have user data passed in, which includes vehicle info now.
    val context = LocalContext.current
    val userRepo = UserProfileRepository
    val executor = ContextCompat.getMainExecutor(context)

    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // State for managing Bottom Sheet content (Vehicle List vs Add Vehicle Form)
    var isAddingVehicle by rememberSaveable { mutableStateOf(false) }
    var editingVehicleId by rememberSaveable { mutableStateOf<String?>(null) }
    var showVehicleSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Form states for adding vehicle
    var newBrand by rememberSaveable { mutableStateOf("") }
    var newColor by rememberSaveable { mutableStateOf("") }
    var newPlate by rememberSaveable { mutableStateOf("") }

    // Document Upload State
    var grantUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var grantUri = if (grantUriString != null) Uri.parse(grantUriString) else null
    var showCameraOverlay by rememberSaveable { mutableStateOf(false) }
    var showFileTooLargeDialog by rememberSaveable { mutableStateOf(false) }

    // Status Popups
    var showApplicationApprovedDialog by remember { mutableStateOf(false) }
    var showApplicationRejectedDialog by remember { mutableStateOf(false) }
    var showApplicationSubmittedDialog by remember { mutableStateOf(false) }
    var isSubmittingApplication by remember { mutableStateOf(false) }

    // Real-time applications from DB
    val currentUid = user.uid
    var applications by remember { mutableStateOf<List<Vehicle>>(emptyList()) }
    var lastApplicationsStates by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Selected vehicle plate
    val selectedPlate = remember { mutableStateOf(user.vehiclePlateNumber) }
    LaunchedEffect(user.vehiclePlateNumber) {
        selectedPlate.value = user.vehiclePlateNumber
    }

    val formScrollState = rememberScrollState()

    // --- Permissions ---
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showCameraOverlay = true
        else Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (isFileTooLarge(context, it)) {
                showFileTooLargeDialog = true
            } else {
                grantUriString = it.toString()
            }
        }
    }

    // --- Real-time Listener ---
    DisposableEffect(currentUid) {
        if (currentUid.isEmpty()) return@DisposableEffect onDispose {}

        val registration = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("newVehicleApplications")
            .whereEqualTo("userId", currentUid)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val newApps = snapshot.documents.mapNotNull { doc ->
                    try {
                        Vehicle(
                            id = doc.getString("id") ?: doc.id,
                            brand = doc.getString("brand") ?: "",
                            color = doc.getString("color") ?: "",
                            plateNumber = doc.getString("plateNumber") ?: "",
                            licenseNumber = doc.getString("licenseNumber") ?: "",
                            grantUrl = doc.getString("grantUrl") ?: "",
                            status = doc.getString("status") ?: "Pending"
                        )
                    } catch (ex: Exception) {
                        null
                    }
                }

                // Detect transitions for popups
                newApps.forEach { app ->
                    val lastStatus = lastApplicationsStates[app.id]
                    if (lastStatus == "Pending") {
                        if (app.status == "Approved") {
                            showApplicationApprovedDialog = true
                            scope.launch {
                                userRepo.addNewVehicle(app)
                                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    .collection("newVehicleApplications")
                                    .document(app.plateNumber)
                                    .delete()
                            }
                        } else if (app.status == "Rejected") {
                            showApplicationRejectedDialog = true
                        }
                    }
                }

                applications = newApps
                lastApplicationsStates = newApps.associate { it.id to it.status }
            }

        onDispose { registration.remove() }
    }

    fun onCameraCaptured(uri: Uri) {
        if (isFileTooLarge(context, uri)) {
            showFileTooLargeDialog = true
        } else {
            grantUriString = uri.toString()
        }
        showCameraOverlay = false
    }

    if (showVehicleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVehicleSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFFF5F7FB)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .let { if (isAddingVehicle) it.height(500.dp) else it.wrapContentHeight() }
            ) {
                if (isAddingVehicle) {
                    // --- ADD NEW VEHICLE FORM ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .verticalScroll(formScrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (editingVehicleId != null) "Resubmit Vehicle Details" else "Register New Vehicle",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = newBrand,
                            onValueChange = { newBrand = it },
                            label = { Text("Car Brand & Model") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = newColor,
                            onValueChange = { newColor = it },
                            label = { Text("Car Color") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = newPlate,
                            onValueChange = { newPlate = it },
                            label = { Text("Plate Number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))

                        // DOCUMENT UPLOAD SECTION
                        DocumentCard(
                            label = "Vehicle Grant Document",
                            imageUri = grantUri,
                            boxHeight = 140.dp,
                            onCapture = {
                                scope.launch { formScrollState.animateScrollTo(formScrollState.maxValue) }
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            onPick = {
                                scope.launch { formScrollState.animateScrollTo(formScrollState.maxValue) }
                                galleryLauncher.launch("image/*")
                            },
                            onRemove = { grantUriString = null }
                        )

                        Spacer(Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    isAddingVehicle = false
                                    editingVehicleId = null
                                    newBrand = ""; newColor = ""; newPlate = ""; grantUriString =
                                    null
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isSubmittingApplication
                            ) {
                                Text("Cancel", color = Color.Gray)
                            }
                            Spacer(Modifier.width(16.dp))
                            Button(
                                onClick = {
                                    if (newBrand.isBlank() || newColor.isBlank() || newPlate.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "Please fill in all details",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }
                                    if (grantUri == null && editingVehicleId == null) {
                                        Toast.makeText(
                                            context,
                                            "Please upload Vehicle Grant",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }

                                    isSubmittingApplication = true
                                    scope.launch {
                                        try {
                                            val currentMatric = user.matricNumber

                                            if (editingVehicleId != null) {
                                                // RESUBMIT REJECTED
                                                val originalInList =
                                                    user.vehicles.find { it.id == editingVehicleId }
                                                val originalInApps =
                                                    applications.find { it.id == editingVehicleId }
                                                val original = originalInList ?: originalInApps

                                                var uploadedUrl = original?.grantUrl ?: ""
                                                val isNewUpload =
                                                    grantUri != null && (grantUri!!.scheme != "http" && grantUri!!.scheme != "https")

                                                if (isNewUpload) {
                                                    uploadedUrl = userRepo.uploadVehicleGrant(
                                                        user.uid,
                                                        grantUri!!
                                                    )
                                                }

                                                val updatedVehicle = Vehicle(
                                                    id = editingVehicleId!!,
                                                    brand = newBrand.trim(),
                                                    color = newColor.trim(),
                                                    plateNumber = newPlate.trim().uppercase(),
                                                    licenseNumber = original?.licenseNumber
                                                        ?: user.licenseNumber,
                                                    grantUrl = uploadedUrl,
                                                    status = "Pending",
                                                    lastEditedAt = System.currentTimeMillis()
                                                )

                                                if (userRepo.submitVehicleApplication(
                                                        currentMatric,
                                                        updatedVehicle
                                                    )
                                                ) {
                                                    showApplicationSubmittedDialog = true
                                                    isAddingVehicle = false
                                                    editingVehicleId = null
                                                    newBrand = ""; newColor = ""; newPlate =
                                                        ""; grantUriString = null
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Failed to submit",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                // APPLY NEW
                                                val uploadedUrl = userRepo.uploadVehicleGrant(
                                                    user.uid,
                                                    grantUri!!
                                                )
                                                val newVeh = Vehicle(
                                                    id = UUID.randomUUID().toString(),
                                                    brand = newBrand.trim(),
                                                    color = newColor.trim(),
                                                    plateNumber = newPlate.trim().uppercase(),
                                                    licenseNumber = user.licenseNumber,
                                                    grantUrl = uploadedUrl,
                                                    status = "Pending",
                                                    lastEditedAt = 0L
                                                )
                                                if (userRepo.submitVehicleApplication(
                                                        currentMatric,
                                                        newVeh
                                                    )
                                                ) {
                                                    showApplicationSubmittedDialog = true
                                                    isAddingVehicle = false
                                                    newBrand = ""; newColor = ""; newPlate =
                                                        ""; grantUriString = null
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Failed to apply",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "Error: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } finally {
                                            isSubmittingApplication = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CBlue),
                                enabled = !isSubmittingApplication
                            ) {
                                if (isSubmittingApplication) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(if (editingVehicleId != null) "Resubmit Application" else "Apply New Vehicle")
                                }
                            }
                        }
                    }
                } else {
                    // --- SELECT VEHICLE LIST ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    ) {
                        Text(
                            text = "Select Vehicle",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.height(16.dp))

                        user?.vehicles?.let { approvedVehicles ->
                            val combinedVehicles = remember(approvedVehicles, applications) {
                                val list = approvedVehicles.toMutableList()
                                applications.forEach { app ->
                                    if (list.none { it.plateNumber == app.plateNumber }) {
                                        list.add(app)
                                    }
                                }
                                list
                            }

                            var tempSelected by remember(selectedPlate.value) {
                                mutableStateOf(
                                    selectedPlate.value
                                )
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp)
                            ) {
                                items(combinedVehicles.size) { index ->
                                    val vehicle = combinedVehicles[index]
                                    val isSelected = tempSelected == vehicle.plateNumber

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (vehicle.status == "Approved") tempSelected =
                                                    vehicle.plateNumber
                                            }
                                            .padding(vertical = 12.dp, horizontal = 8.dp)
                                            .background(
                                                if (isSelected) CBlue.copy(alpha = 0.1f) else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(8.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                if (vehicle.status == "Approved") tempSelected =
                                                    vehicle.plateNumber
                                            },
                                            enabled = vehicle.status == "Approved"
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "${vehicle.brand}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = vehicle.plateNumber + " • " + vehicle.color,
                                                fontSize = 14.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        if (isSelected) {
                                            Spacer(Modifier.weight(1f))
                                            Text(
                                                "Active",
                                                color = CBlue,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else if (vehicle.status == "Pending") {
                                            Spacer(Modifier.weight(1f))
                                            Text(
                                                "Pending",
                                                color = Color(0xFFFFA000),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else if (vehicle.status == "Rejected") {
                                            Spacer(Modifier.weight(1f))
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    "Rejected",
                                                    color = Color.Red,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    "Tap to Resubmit",
                                                    color = CBlue,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.clickable {
                                                        newBrand = vehicle.brand; newColor =
                                                        vehicle.color; newPlate =
                                                        vehicle.plateNumber
                                                        grantUriString = null; editingVehicleId =
                                                        vehicle.id; isAddingVehicle = true
                                                    })
                                            }
                                        }

                                        if (vehicle.status == "Approved") {
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        if (userRepo.deleteVehicle(vehicle.id)) {
                                                            Toast.makeText(
                                                                context,
                                                                "Vehicle Deleted",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Color.Red
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                }

                                item {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isAddingVehicle = true }
                                            .padding(vertical = 16.dp, horizontal = 16.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = CBlue
                                        )
                                        Spacer(Modifier.width(16.dp))
                                        Text(
                                            "Add New Vehicle",
                                            color = CBlue,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    scope.launch {
                                        if (tempSelected != selectedPlate.value) {
                                            val success = userRepo.switchVehicle(tempSelected)
                                            if (success) {
                                                selectedPlate.value = tempSelected
                                                Toast.makeText(
                                                    context,
                                                    "Vehicle Switched",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                scope.launch { sheetState.hide() }
                                                    .invokeOnCompletion { showVehicleSheet = false }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to switch",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            scope.launch { sheetState.hide() }
                                                .invokeOnCompletion { showVehicleSheet = false }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Confirm Selection", fontSize = 16.sp)
                            }

                        } ?: run {
                            Text(
                                "No vehicles found",
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Button(
                                onClick = { isAddingVehicle = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Add First Vehicle")
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Profile", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CBlue)
            )
        },
        bottomBar = {
            // Reusing the Driver Bottom Bar, but assuming we can handle navigation back to dashboard
            BottomNavigationBarDriver(
                selectedIndex = selectedNavIndex, // 3 is usually Profile if we added it, but let's assume valid index is passed
                onSelected = { index ->
                    when (index) {
                        0 -> navController.navigate(NavRoutes.DriverDashboard.route) {
                            popUpTo(NavRoutes.DriverDashboard.route) { inclusive = true }
                        }

                        1 -> navController.navigate(NavRoutes.DriverScore.route)
                        2 -> navController.navigate(NavRoutes.DriverEarning.route)
                        3 -> { /* Already here */
                        }
                    }
                }
            )
        }
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
                        Text(
                            "@${user.matricNumber}",
                            fontSize = 16.sp,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                        Text(
                            "Driver Account",
                            fontSize = 14.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                Button(
                    onClick = {
                        scope.launch {
                            authViewModel.switchActiveRole("customer")
                            // Navigation is handled by AppNavGraph observing activeRole
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CBlue)
                ) {
                    Text(
                        text = "Switch to Customer Mode",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
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

            // Vehicle Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CBlue)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Vehicle Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(12.dp))
                        ReadOnlyField(
                            label = "Car Brand & Model",
                            value = user.carBrand.ifEmpty { "Not specified" })
                        Spacer(Modifier.height(12.dp))
                        ReadOnlyField(
                            label = "Car Color",
                            value = user.carColor.ifEmpty { "Not specified" })
                        Spacer(Modifier.height(12.dp))
                        ReadOnlyField(
                            label = "Plate Number",
                            value = user.vehiclePlateNumber.ifEmpty { "Not specified" })
                        Spacer(Modifier.height(12.dp))
                        ReadOnlyField(
                            label = "License Number",
                            value = user.licenseNumber.ifEmpty { "Not specified" })
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { showVehicleSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CBlue)
                ) {
                    Text("Change Vehicle", color = CBlue, fontWeight = FontWeight.Bold)
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CBlue)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Contact Information",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(12.dp))
                        ReadOnlyField(label = "Email", value = user.email)
                        Spacer(Modifier.height(12.dp))
                        ReadOnlyField(label = "Phone Number", value = user.phoneNumber)
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

        if (showCameraOverlay) {
            CameraOverlay(
                onCaptured = { uri -> onCameraCaptured(uri) },
                onCancel = { showCameraOverlay = false },
                executor = executor
            )
        }

        if (showCameraOverlay) {
            CameraOverlay(
                onCaptured = { uri -> onCameraCaptured(uri) },
                onCancel = { showCameraOverlay = false },
                executor = executor
            )
        }

        if (showFileTooLargeDialog) {
            AlertDialog(
                onDismissRequest = { showFileTooLargeDialog = false },
                title = { Text("File Too Large") },
                text = { Text("The selected file is too large. Please select an image smaller than 5MB.") },
                confirmButton = {
                    Button(onClick = { showFileTooLargeDialog = false }) { Text("OK") }
                }
            )
        }

        if (showApplicationApprovedDialog) {
            AlertDialog(
                onDismissRequest = { showApplicationApprovedDialog = false },
                title = { Text("Application Approved!", fontWeight = FontWeight.Bold) },
                text = { Text("Your vehicle has been approved and added to the list") },
                confirmButton = {
                    Button(onClick = {
                        showApplicationApprovedDialog = false
                    }) { Text("Great!") }
                }
            )
        }

        if (showApplicationRejectedDialog) {
            AlertDialog(
                onDismissRequest = { showApplicationRejectedDialog = false },
                title = {
                    Text(
                        "Application Rejected",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = { Text("Your application was rejected, please resubmit vehicle details") },
                confirmButton = {
                    Button(onClick = {
                        showApplicationRejectedDialog = false
                    }) { Text("Resubmit") }
                }
            )
        }

        if (showApplicationSubmittedDialog) {
            AlertDialog(
                onDismissRequest = { showApplicationSubmittedDialog = false },
                title = { Text("Submitted", fontWeight = FontWeight.Bold) },
                text = { Text("Application submitted successfully, pending admin approval") },
                confirmButton = {
                    Button(onClick = {
                        showApplicationSubmittedDialog = false
                    }) { Text("OK") }
                }
            )
        }
    }
}
