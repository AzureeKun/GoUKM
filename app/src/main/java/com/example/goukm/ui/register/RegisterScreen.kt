package com.example.goukm.ui.register

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import com.example.goukm.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

val CBlue = Color(0xFF6b87c0)
val CRed = Color(0xFFE53935)

val CanvaSansBold = FontFamily(
    Font(R.font.canva_sans_bold, FontWeight.Bold)
)
val CanvaSansRegular = FontFamily(
    Font(R.font.canva_sans_regular)
)
val PoppinsLight = FontFamily(
    Font(R.font.poppins_light)
)

@Composable
fun RegisterScreen(
    modifier: Modifier,
    onNavigateToName: () -> Unit = {},
    onLoginSuccess: (String) -> Unit = {}
) {

    var email by remember { mutableStateOf("") }
    var phoneNum by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // ERROR STATES
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    fun validateFields(): Boolean {
        var valid = true

        // ✦ Reset error first
        emailError = null
        phoneError = null
        passwordError = null

        // ✦ EMAIL VALIDATION
        val domain = "@siswa.ukm.edu.my"
        if (email.isBlank()) {
            emailError = "Email cannot be empty."
            valid = false
        } else if (!email.endsWith(domain, ignoreCase = true)) {
            emailError = "Please enter your valid student email."
            valid = false
        } else {
            val matric = email.substring(0, email.length - domain.length)
            if (matric.length != 7 || !matric.startsWith("A", ignoreCase = true)) {
                emailError = "Please enter your valid student email."
                valid = false
            }
        }

        // ✦ PHONE VALIDATION
        if (phoneNum.isBlank()) {
            phoneError = "Phone number cannot be empty."
            valid = false
        } else if (!phoneNum.all { it.isDigit() }) {
            phoneError = "Phone number must be digits only."
            valid = false
        } else if (phoneNum.length !in 10..11) {
            phoneError = "Phone number must be 10–11 digits."
            valid = false
        }

        // ✦ PASSWORD VALIDATION
        if (password.isBlank()) {
            passwordError = "Password cannot be empty."
            valid = false
        } else if (password.length !in 6..20) {
            passwordError = "Password must be 6–20 characters."
            valid = false
        } else if (!password.any { it.isUpperCase() }) {
            passwordError = "Must contain at least 1 capital letter."
            valid = false
        } else if (!password.any { it.isDigit() }) {
            passwordError = "Must contain at least 1 number."
            valid = false
        }

        return valid
    }

    Column(
        modifier = Modifier
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
            text = "Enter your detail",
            color = Color.Black,
            fontSize = 12.sp,
            fontFamily = CanvaSansRegular
        )

        Spacer(Modifier.height(32.dp))

        // ===============================
        // EMAIL FIELD + ERROR
        // ===============================
        TextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            label = { Text("Email", fontFamily = PoppinsLight) },
            isError = emailError != null,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (emailError != null) {
            Text(
                text = emailError!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(12.dp))


        // ===============================
        // PHONE FIELD + ERROR
        // ===============================
        TextField(
            value = phoneNum,
            onValueChange = {
                phoneNum = it
                phoneError = null
            },
            label = { Text("Phone Number", fontFamily = PoppinsLight) },
            isError = phoneError != null,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (phoneError != null) {
            Text(
                text = phoneError!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(12.dp))


        // ===============================
        // PASSWORD FIELD + ERROR
        // ===============================
        var passwordVisible by remember { mutableStateOf(false) }

        TextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
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
                        contentDescription = null
                    )
                }
            }
        )

        if (passwordError != null) {
            Text(
                text = passwordError!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))

        // ===============================
        // BUTTON
        // ===============================
        Button(
            onClick = {
                if (!validateFields()) return@Button

                // Your existing logic continues...
                RegistrationState.email = email
                RegistrationState.password = password
                RegistrationState.phoneNumber = phoneNum

                scope.launch {
                    // LOGIN / REGISTER logic unchanged
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CRed,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "Continue", fontFamily = PoppinsLight, fontSize = 16.sp)
        }
    }
}
