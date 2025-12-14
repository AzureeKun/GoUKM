package com.example.goukm.ui.register

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.goukm.R

@Composable
fun NamePage(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var name by remember { mutableStateOf(RegistrationState.name) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CBlue)
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.goukm_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(200.dp)
        )

        Spacer(Modifier.height(32.dp))

        // Title
        Text(
            text = "REGISTER ACCOUNT",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CanvaSansRegular,
            textAlign = TextAlign.Center
        )

        // Subtitle
        Text(
            text = "Your name will be displayed publicly",
            color = Color.Black,
            fontSize = 11.sp,
            fontFamily = CanvaSansRegular,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("please enter your name", fontFamily = PoppinsLight) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Continue Button
        Button(
            onClick = {
                RegistrationState.name = name
                // ✅ Navigate to RegisterOption
                navController.navigate("register_option")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CBlack,
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