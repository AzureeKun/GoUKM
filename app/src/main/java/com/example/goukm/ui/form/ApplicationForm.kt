package com.example.goukm.ui.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.imePadding
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
import com.example.goukm.util.DriverEligibilityChecker
import com.example.goukm.util.EligibilityResult
import androidx.compose.ui.platform.LocalContext

// Assuming CBlue is defined in the package scope, but defining locally for safety
import com.example.goukm.ui.theme.CBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverApplicationFormScreen(
    navController: NavHostController,
    onApplicationSubmit: () -> Unit, // Callback to navigate away on success
    authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(LocalContext.current)
    ),
    applicationViewModel: DriverApplicationViewModel = viewModel()
) {
    // --- State for Form Fields ---
    var licenseNumber by remember { mutableStateOf(applicationViewModel.licenseNumber) }
    var vehiclePlateNumber by remember { mutableStateOf(applicationViewModel.vehiclePlateNumber) }
    var carBrand by remember { mutableStateOf(applicationViewModel.carBrand) }
    var carColor by remember { mutableStateOf(applicationViewModel.carColor) }
    val applicationStatus by authViewModel.driverApplicationStatus.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val carBrands = listOf("Perodua", "Proton", "Honda", "Toyota", "Nissan", "Mazda", "Mitsubishi", "Kia", "Hyundai", "BMW", "Mercedes-Benz", "Ford", "Subaru", "Volkswagen", "Other")
    val carColors = listOf("White", "Black", "Silver", "Grey", "Red", "Blue", "Green", "Yellow", "Orange", "Brown", "Others")

    // Check eligibility
    val eligibilityResult = remember(currentUser) {
        currentUser?.let { DriverEligibilityChecker.checkEligibility(it) }
            ?: EligibilityResult(isEligible = false, reason = "User profile not available")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Step 1 of 3: Enter Vehicle Details", color = Color.White) },
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
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Show eligibility error if not eligible
            if (!eligibilityResult.isEligible) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Not Eligible to Apply",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                eligibilityResult.reason,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                DriverEligibilityChecker.getEligibilityRequirementsMessage(),
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            if (applicationStatus == "under_review") {
                item {
                    Text(
                        "Your application is currently under review. You will be notified once it is approved.",
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

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
                // --- Car Brand Selection ---
                SelectionDropdown(
                    label = "Car Brand",
                    selectedOption = carBrand,
                    onOptionSelected = { carBrand = it },
                    options = carBrands
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                // --- Car Color Selection ---
                SelectionDropdown(
                    label = "Car Color",
                    selectedOption = carColor,
                    onOptionSelected = { carColor = it },
                    options = carColors
                )
                Spacer(Modifier.height(16.dp))
            }


            item {
                // --- Submission Button ---
                Button(
                    onClick = {
                        if (licenseNumber.isNotBlank() && vehiclePlateNumber.isNotBlank() && carBrand.isNotBlank() && carColor.isNotBlank()) {
                            applicationViewModel.setVehicleInfo(
                                license = licenseNumber,
                                plate = vehiclePlateNumber,
                                brand = carBrand,
                                color = carColor
                            )
                                    onApplicationSubmit()
                        }
                    },
                    enabled = eligibilityResult.isEligible &&
                            licenseNumber.isNotBlank() &&
                            vehiclePlateNumber.isNotBlank() &&
                            carBrand.isNotBlank() &&
                            carColor.isNotBlank() &&
                            applicationStatus != "under_review",
                    colors = ButtonDefaults.buttonColors(containerColor = CBlue),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (applicationStatus == "under_review") "Application Pending"
                        else "Next",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionDropdown(
    label: String,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    options: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
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
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
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