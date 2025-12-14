package com.example.goukm.ui.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.Credential
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.goukm.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

val CBlue = Color(0xFF6b87c0)
val CRed = Color(0xFFE53935)

val CanvaSansBold = FontFamily(Font(R.font.canva_sans_bold, FontWeight.Bold))
val CanvaSansRegular = FontFamily(Font(R.font.canva_sans_regular))
val PoppinsLight = FontFamily(Font(R.font.poppins_light))

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onNavigateToName: () -> Unit = {},
    onLoginSuccess: (String) -> Unit = {},
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(LocalContext.current))
) {
    var matricNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var matricError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun validateFields(): Boolean {
        var valid = true
        matricError = null
        passwordError = null
        generalError = null

        // MATRIC NUMBER VALIDATION
        val normalizedMatric = matricNumber.trim().uppercase()
        if (normalizedMatric.isBlank()) {
            matricError = "Matriculation number cannot be empty"
            valid = false
        } else if (normalizedMatric.length != 7) {
            matricError = "Matriculation number must be 7 characters (e.g., A203399)"
            valid = false
        } else if (!normalizedMatric.startsWith("A", ignoreCase = true)) {
            matricError = "Matriculation number must start with 'A'"
            valid = false
        } else if (!normalizedMatric.substring(1).all { it.isDigit() }) {
            matricError = "Matriculation number must be A followed by 6 digits"
            valid = false
        }

        // PASSWORD VALIDATION
        if (password.isBlank()) {
            passwordError = "Password cannot be empty"
            valid = false
        } else if (password.length < 6 || password.length > 20) {
            passwordError = "Password must be 6–20 characters"
            valid = false
        } else if (!password.any { it.isUpperCase() }) {
            passwordError = "Password must contain at least 1 uppercase letter"
            valid = false
        } else if (!password.any { it.isDigit() }) {
            passwordError = "Password must contain at least 1 number"
            valid = false
        }

        return valid
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CBlue)
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.goukm_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(200.dp)
        )

        Spacer(Modifier.height(32.dp))

        Text(
            text = "REGISTER ACCOUNT",
            color = Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CanvaSansBold
        )

        Text(
            text = "Enter your details",
            color = Color.Black,
            fontSize = 12.sp,
            fontFamily = CanvaSansRegular
        )

        Spacer(Modifier.height(32.dp))

        // MATRIC NUMBER
        TextField(
            value = matricNumber,
            onValueChange = { matricNumber = it; matricError = null },
            label = { Text("Matrics Number", fontFamily = PoppinsLight) },
            placeholder = { Text("e.g., A203399", fontFamily = PoppinsLight) },
            isError = matricError != null,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        matricError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

        Spacer(Modifier.height(12.dp))

        // PASSWORD
        TextField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            label = { Text("Password", fontFamily = PoppinsLight) },
            isError = passwordError != null,
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if(passwordVisible) "Hide password" else "Show password"
                    )
                }
            }
        )
        passwordError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

        Spacer(Modifier.height(24.dp))

        generalError?.let { Text(it, color = Color.Red, fontSize = 14.sp) }

        Button(
            onClick = {
                if (!validateFields()) return@Button

                isLoading = true
                val normalizedMatric = matricNumber.trim().uppercase()
                val email = "${normalizedMatric.lowercase()}@siswa.ukm.edu.my"
                RegistrationState.email = email
                RegistrationState.password = password

                scope.launch {
                    try {
                        // Authenticate using matric number and password
                        // This will check GoUKM database first, then Mock SMPWeb if not found
                        val res = try {
                            RegistrationRepository.loginUser(normalizedMatric, password)
                        } catch (se: SecurityException) {
                            // Fallback for debug / broker issue
                            println("⚠️ SecurityException caught: ${se.message}")
                            RegistrationRepository.loginUserWithoutBroker(normalizedMatric, password)
                        }

                        if (res.isSuccess) {
                            // User exists in GoUKM - login successful
                            val uid = res.getOrNull()!!
                            authViewModel.handleLoginSuccess(uid)
                            authViewModel.fetchUserProfile(defaultToCustomer = true)
                            onLoginSuccess("customer")
                        } else {
                            val error = res.exceptionOrNull()
                            val errorMessage = error?.message ?: ""
                            
                            // Check if this is a new user from Mock SMPWeb
                            if (errorMessage.startsWith("NEW_USER_FROM_SMPWEB:")) {
                                // Extract matric and store student data for registration flow
                                val student = RegistrationRepository.checkSMPWebExists(normalizedMatric)
                                if (student != null) {
                                    // Store student data in RegistrationState for use in NamePage/RegisterOption
                                    RegistrationState.email = student.email
                                    RegistrationState.phoneNumber = student.phoneNumber
                                    RegistrationState.name = student.fullName
                                    RegistrationState.smpWebStudent = student // Store full student data
                                    
                                    // Navigate to NamePage for registration
                                    onNavigateToName()
                                } else {
                                    generalError = "Student data not found. Please try again."
                                }
                            } else {
                                // Handle other errors
                                when {
                                    errorMessage.contains("Invalid matriculation number", ignoreCase = true) -> {
                                        matricError = "Invalid matriculation number"
                                    }
                                    errorMessage.contains("Invalid password", ignoreCase = true) -> {
                                        passwordError = "Wrong password. Please re-enter your password."
                                    }
                                    errorMessage.contains("wrong-password", ignoreCase = true) -> {
                                        passwordError = "Wrong password. Please re-enter your password."
                                    }
                                    else -> {
                                        generalError = errorMessage.ifEmpty { "Authentication failed. Please try again." }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        generalError = "An error occurred: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CRed, contentColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Continue", fontFamily = PoppinsLight, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}
