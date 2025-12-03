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
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(LocalContext.current))
) {
    var email by remember { mutableStateOf("") }
    var phoneNum by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showPhoneMismatchDialog by remember { mutableStateOf(false) }


    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun validateFields(): Boolean {
        var valid = true
        emailError = null
        phoneError = null
        passwordError = null
        generalError = null

        val domain = "@siswa.ukm.edu.my"

        // EMAIL
        if (email.isBlank()) {
            emailError = "Email cannot be empty"
            valid = false
        } else if (!email.endsWith(domain, ignoreCase = true)) {
            emailError = "Email must end with $domain"
            valid = false
        } else {
            val matric = email.substringBefore(domain)
            if (matric.length != 7 || !matric.startsWith("A", ignoreCase = true)) {
                emailError = "Invalid student matric number"
                valid = false
            }
        }

        // PHONE
        if (phoneNum.isBlank()) {
            phoneError = "Phone number cannot be empty"
            valid = false
        } else if (!phoneNum.all { it.isDigit() }) {
            phoneError = "Phone number must contain digits only"
            valid = false
        } else if (phoneNum.length !in 10..11) {
            phoneError = "Phone number must be 10–11 digits"
            valid = false
        }

        // PASSWORD
        if (password.isBlank()) {
            passwordError = "Password cannot be empty"
            valid = false
        } else if (password.length !in 6..20) {
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
    if (showPhoneMismatchDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneMismatchDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = { Text("Phone Number Already Exists") },
            text = {
                Text(
                    "This phone number already exists but is registered under a different email.\n\n" +
                            "Choose an option below:",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.End
                ){

                    TextButton(
                        onClick = {showPhoneMismatchDialog = false},
                        modifier = Modifier.weight(1f)
                    ){
                        Text(
                            "Re-enter Details",
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            showPhoneMismatchDialog = false
                            onNavigateToName()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CBlue
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ){
                        Text(
                            "Register",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            dismissButton = {}
        )
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

        // EMAIL
        TextField(
            value = email,
            onValueChange = { email = it; emailError = null },
            label = { Text("Email", fontFamily = PoppinsLight) },
            isError = emailError != null,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        emailError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

        Spacer(Modifier.height(12.dp))

        // PHONE
        TextField(
            value = phoneNum,
            onValueChange = { phoneNum = it; phoneError = null },
            label = { Text("Phone Number", fontFamily = PoppinsLight) },
            isError = phoneError != null,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        phoneError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

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
                RegistrationState.email = email
                RegistrationState.password = password
                RegistrationState.phoneNumber = phoneNum

                scope.launch {
                    try {
                        // 🔍 CHECK IF PHONE NUMBER EXISTS IN ANY ACCOUNT
                        val phoneQuery = FirebaseFirestore.getInstance()
                            .collection("users")
                            .whereEqualTo("phoneNumber", phoneNum.trim())
                            .get()
                            .await()

// Phone exists but email not same = suspicious = reject
                        if (!phoneQuery.isEmpty) {
                            val phoneOwnerEmail = phoneQuery.documents.first().getString("email") ?: ""

                            if (phoneOwnerEmail.lowercase() != email.lowercase()) {
                                // Show dialog instead of showing error text
                                showPhoneMismatchDialog = true

                                isLoading = false
                                return@launch
                            }
                        }

                        val exists = RegistrationRepository.checkEmailExists(email)

                        if (exists) {
                            // LOGIN USER
                            val res = RegistrationRepository.loginUser(email, password)
                            if (res.isSuccess) {
                                val uid = res.getOrNull()!!

                                // 🔹 Check phone number BEFORE login success
                                val doc = FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .get()
                                    .await()

                                val storedPhone = (doc.getString("phoneNumber") ?: "").trim()
                                if (storedPhone != phoneNum.trim()) {
                                    phoneError = "Phone number does not match our records"
                                    isLoading = false
                                    return@launch
                                }

                                // 🔹 Only now mark login success
                                authViewModel.handleLoginSuccess(uid)
                                val role = doc.getString("role") ?: "customer"
                                onLoginSuccess(role)
                            } else {
                                passwordError = "Wrong password. Please re-enter your password."
                            }
                        } else {
                            // NEW USER -> NAVIGATE TO NAME REGISTRATION
                            onNavigateToName()
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
