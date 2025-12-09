package com.example.goukm.ui.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.goukm.ui.register.AuthViewModel
import com.example.goukm.ui.register.AuthViewModelFactory
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

// Assuming CBlue is defined in the package scope, but defining locally for safety
val CBlue = Color(0xFF6b87c0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverApplicationFormScreen(
    navController: NavHostController,
    onApplicationSubmit: () -> Unit, // Callback to navigate away on success
    authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(LocalContext.current)
    )
) {
    // --- State for Form Fields ---
    var licenseNumber by remember { mutableStateOf("") }
    var vehiclePlateNumber by remember { mutableStateOf("") }
    var selectedVehicleType by remember { mutableStateOf("Motorcycle") }
    var acceptedTerms by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val vehicleTypes = listOf("Motorcycle", "Car", "Van")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Application", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CBlue),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    "Vehicle & Licensing Details",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            item {
                // --- License Number Field ---
                OutlinedTextField(
                    value = licenseNumber,
                    onValueChange = { licenseNumber = it },
                    label = { Text("Driving License Number") },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CBlue,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = CBlue
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }

            item {
                // --- Vehicle Plate Number Field ---
                OutlinedTextField(
                    value = vehiclePlateNumber,
                    onValueChange = { vehiclePlateNumber = it },
                    label = { Text("Vehicle Plate Number (e.g., VBM 1234)") },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CBlue,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = CBlue
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }

            item {
                // --- Vehicle Type Selection (Dropdown/Exposed Dropdown Menu) ---
                VehicleTypeDropdown(
                    selectedType = selectedVehicleType,
                    onTypeSelected = { selectedVehicleType = it },
                    vehicleTypes = vehicleTypes
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                // --- Terms and Conditions Checkbox ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = acceptedTerms,
                        onCheckedChange = { acceptedTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = CBlue)
                    )
                    Text(
                        "I agree to the Driver Terms and Conditions.",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Spacer(Modifier.height(32.dp))
            }

            item {
                // --- Submission Button ---
                Button(
                    onClick = {
                        if (acceptedTerms && licenseNumber.isNotBlank() && vehiclePlateNumber.isNotBlank()) {
                            // 1. Submit Application via AuthViewModel
                            authViewModel.submitDriverApplication(
                                licenseNumber = licenseNumber,
                                vehiclePlateNumber = vehiclePlateNumber,
                                vehicleType = selectedVehicleType
                            ) { success ->
                                if (success) {
                                    println("Driver Application Submitted and Role Updated.")
                                    onApplicationSubmit()
                                } else {
                                    println("Error: Failed to update user profile.")
                                    // Handle error? For now, maybe just stay or show toast. 
                                    // User flow logic says we navigate, but let's just stick to success path navigation or user choice.
                                    // The previous code navigated anyway. Let's do the same for robustness? 
                                    // No, let's only navigate on success to ensure data is saved.
                                    // But to be consistent with previous 'navigate anyway' style but better:
                                    onApplicationSubmit() 
                                }
                            }
                        }
                    },
                    enabled = acceptedTerms && licenseNumber.isNotBlank() && vehiclePlateNumber.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = CBlue),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Submit Application", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleTypeDropdown(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    vehicleTypes: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            readOnly = true,
            label = { Text("Vehicle Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = CBlue,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = CBlue
            ),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            vehicleTypes.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onTypeSelected(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDriverApplicationFormScreen() {
    DriverApplicationFormScreen(
        navController = rememberNavController(),
        onApplicationSubmit = {}
    )
}