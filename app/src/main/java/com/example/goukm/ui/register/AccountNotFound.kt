package com.example.goukm.ui.register
/*
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
import androidx.compose.ui.text.style.TextAlign
import com.example.goukm.R

@Composable
fun AccountNotFound(modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var phoneNum by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }


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
            text = "ACCOUNT NOT FOUND",
            color = Color.Red,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CanvaSansBold
        )

        Text(
            text = "please re enter your details",
            color = Color.Black,
            fontSize = 12.sp,
            fontFamily = com.example.goukm.ui.login.CanvaSansRegular
        )

        Spacer(Modifier.height(32.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("email", fontFamily = PoppinsLight) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        TextField(
            value = phoneNum,
            onValueChange = { phoneNum = it },
            label = { Text("Phone Number", fontFamily = PoppinsLight) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("password", fontFamily = com.example.goukm.ui.login.PoppinsLight) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if(passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            }
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if(!email.endsWith("@siswa.ukm.edu.my", ignoreCase = true)){
                    println("Error: Email must end with @siswa.ukm.edu.my")
                    return@Button
                }

                registerUserAndSave(
                    email = email,
                    password = password,
                    phoneNumber = phoneNum,
                    onSuccess = {
                        println("User registered!")
                    },
                    onFailure = { errorMessage ->
                        println("Registeration Failed: $errorMessage")
                    }
                )
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CRed,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Continue",
                fontFamily = PoppinsLight,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun AccountNotFoundPreview() {
    Scaffold { paddingValues ->
        AccountNotFound (modifier = Modifier.padding(paddingValues))
    }
}*/