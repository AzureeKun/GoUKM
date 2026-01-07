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

    // State for managing Bottom Sheet content (Vehicle List vs Add Vehicle Form)
    var isAddingVehicle by remember { mutableStateOf(false) }
    var editingVehicleId by remember { mutableStateOf<String?>(null) }
    var showVehicleSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Form states for adding vehicle
    var newBrand by remember { mutableStateOf("") }
    var newColor by remember { mutableStateOf("") }
    var newPlate by remember { mutableStateOf("") }
    var newLicense by remember { mutableStateOf("") }

    // Selected vehicle plate
    val selectedPlate = remember { mutableStateOf("") }
    LaunchedEffect(user) {
        selectedPlate.value = user?.vehiclePlateNumber ?: ""
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
                            .padding(bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Register New Vehicle",
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

                        OutlinedTextField(
                            value = user?.licenseNumber ?: "",
                            onValueChange = { },
                            label = { Text("License Number (Fixed)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = true // Make it read-only
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
                                    newBrand = ""; newColor = ""; newPlate = ""; newLicense = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", color = Color.Gray)
                            }
                            Spacer(Modifier.width(16.dp))
                            Button(
                                onClick = {
                                    if (newBrand.isNotBlank() && newColor.isNotBlank() && newPlate.isNotBlank()) {
                                        scope.launch {
                                            if (editingVehicleId != null) {
                                                // UPDATE EXISTING
                                                // Always use the User's master license number to ensure consistency.
                                                // We do not trust the individual vehicle record if it differs from the driver's main profile.
                                                val licenseToUse = user?.licenseNumber ?: ""

                                                val  updatedVehicle = Vehicle(
                                                    id = editingVehicleId!!,
                                                    brand = newBrand.trim(),
                                                    color = newColor.trim(),
                                                    plateNumber = newPlate.trim().uppercase(),
                                                    licenseNumber = licenseToUse,
                                                    lastEditedAt = System.currentTimeMillis()
                                                )
                                                val success = userRepo.updateVehicle(updatedVehicle)
                                                if (success) {
                                                    Toast.makeText(context, "Vehicle Updated", Toast.LENGTH_SHORT).show()
                                                    isAddingVehicle = false
                                                    editingVehicleId = null
                                                    newBrand = ""; newColor = ""; newPlate = ""; newLicense = ""
                                                } else {
                                                    Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                // ADD NEW
                                                val newVehicle = Vehicle(
                                                    id = UUID.randomUUID().toString(),
                                                    brand = newBrand.trim(),
                                                    color = newColor.trim(),
                                                    plateNumber = newPlate.trim().uppercase(),
                                                    licenseNumber = user?.licenseNumber ?: "",
                                                    // Set lastEditedAt to 0 ensures "never edited" status
                                                    lastEditedAt = 0L 
                                                )
                                                val success = userRepo.addNewVehicle(newVehicle)
                                                if (success) {
                                                    Toast.makeText(context, "Vehicle Added", Toast.LENGTH_SHORT).show()
                                                    isAddingVehicle = false
                                                    newBrand = ""; newColor = ""; newPlate = ""; newLicense = ""
                                                } else {
                                                    Toast.makeText(context, "Failed to add vehicle", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CBlue)
                            ) {
                                Text(if (editingVehicleId != null) "Update Vehicle" else "Save Vehicle")
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
                        
                        user?.vehicles?.let { vehicles ->
                            var tempSelected by remember(selectedPlate.value) { mutableStateOf(selectedPlate.value) }
                            
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp) 
                            ) {
                                items(vehicles.size) { index ->
                                    val vehicle = vehicles[index]
                                    val isSelected = tempSelected == vehicle.plateNumber
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { tempSelected = vehicle.plateNumber }
                                            .padding(vertical = 12.dp, horizontal = 8.dp)
                                            .background(
                                                if (isSelected) CBlue.copy(alpha = 0.1f) else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(8.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { tempSelected = vehicle.plateNumber }
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
                                        } else {
                                             Spacer(Modifier.weight(1f))
                                        }
                                        
                                        // Edit Button
                                        IconButton(
                                            onClick = {
                                                val oneWeekInMillis = 7 * 24 * 60 * 60 * 1000L
                                                // Allow edit if never edited (0L) or last edit > 1 week ago
                                                val canEdit = vehicle.lastEditedAt == 0L || (System.currentTimeMillis() - vehicle.lastEditedAt) > oneWeekInMillis
                                                
                                                if (canEdit) {
                                                    newBrand = vehicle.brand
                                                    newColor = vehicle.color
                                                    newPlate = vehicle.plateNumber
                                                    newLicense = vehicle.licenseNumber
                                                    editingVehicleId = vehicle.id
                                                    isAddingVehicle = true
                                                } else {
                                                    val daysLeft = 7 - ((System.currentTimeMillis() - vehicle.lastEditedAt) / (24 * 60 * 60 * 1000L))
                                                    Toast.makeText(context, "Can edit in $daysLeft days", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = CBlue
                                            )
                                        }

                                        // Delete Button
                                        IconButton(
                                            onClick = {
                                                // Show simplified alert dialog - for now assume direct action or use a local state for dialog
                                                scope.launch {
                                                    val success = userRepo.deleteVehicle(vehicle.id)
                                                    if (success) {
                                                        Toast.makeText(context, "Vehicle Deleted", Toast.LENGTH_SHORT).show()
                                                        // Refresh profile handled by listener implicitly or manual trigger if needed
                                                        // Actually, Firestore listener in ViewModel should update the UI automatically
                                                    } else {
                                                        Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                }
                                
                                item {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isAddingVehicle = true }
                                            .padding(vertical = 16.dp, horizontal = 16.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add", tint = CBlue)
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
                                                Toast.makeText(context, "Vehicle Switched", Toast.LENGTH_SHORT).show()
                                                scope.launch { sheetState.hide() }.invokeOnCompletion { showVehicleSheet = false }
                                            } else {
                                                Toast.makeText(context, "Failed to switch", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            scope.launch { sheetState.hide() }.invokeOnCompletion { showVehicleSheet = false }
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
                            Text("No vehicles found", modifier = Modifier.align(Alignment.CenterHorizontally))
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

    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
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
                        1 -> navController.navigate(NavRoutes.DriverChatList.route)
                        2 -> navController.navigate(NavRoutes.DriverScore.route)
                        3 -> navController.navigate(NavRoutes.DriverEarning.route)
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
                        Text("@${user.matricNumber}", fontSize = 16.sp, color = Color.Black.copy(alpha = 0.7f))
                        Text("Driver Account", fontSize = 14.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
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
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CBlue)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Vehicle Details", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Spacer(Modifier.height(12.dp))
                        ReadOnlyField(label = "Car Brand & Model", value = user.carBrand.ifEmpty { "Not specified" })
                        Spacer(Modifier.height(12.dp))
                        ReadOnlyField(label = "Car Color", value = user.carColor.ifEmpty { "Not specified" })
                        Spacer(Modifier.height(12.dp))
                        ReadOnlyField(label = "Plate Number", value = user.vehiclePlateNumber.ifEmpty { "Not specified" })
                        Spacer(Modifier.height(12.dp))
                        ReadOnlyField(label = "License Number", value = user.licenseNumber.ifEmpty { "Not specified" })
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
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CBlue)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Contact Information", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
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
    }
}
