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
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.rememberCoroutineScope
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
    import kotlinx.coroutines.launch

    val CBlack = Color(0xFF000000)
    val CWhite = Color(0xFFFFFFFF)

    // ---------------------------------------------
    // MAIN UI SCREEN
    // ---------------------------------------------
    @Composable
    fun RegisterOption(
        modifier: Modifier = Modifier,
        navController: NavController
    ) {
        val scope = rememberCoroutineScope()

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
                text = "YOU WANNA BE A CUSTOMER OR A DRIVER?",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CanvaSansRegular,
                textAlign = TextAlign.Center
            )

            // Subtitle
            Text(
                text = "You may change the mode later",
                color = Color.Black,
                fontSize = 10.sp,
                fontFamily = CanvaSansRegular,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Passenger Button
            Button(
                onClick = {
                    scope.launch {
                        val res = RegistrationRepository.createUserWithRole(
                            RegistrationState.email,
                            RegistrationState.password,
                            RegistrationState.phoneNumber,
                            RegistrationState.name,
                            "customer"
                        )

                        if (res.isSuccess) {
                            // ✅ Navigate to Customer Dashboard
                            navController.navigate("customer_dashboard") {
                                popUpTo("register_screen") { inclusive = true } // remove registration screens from back stack
                            }
                        } else {
                            println("Error: ${res.exceptionOrNull()?.message}")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CWhite,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "CUSTOMER",
                    fontFamily = PoppinsLight,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // Driver Button
            Button(
                onClick = {
                    scope.launch {
                        val res = RegistrationRepository.createUserWithRole(
                            RegistrationState.email,
                            RegistrationState.password,
                            RegistrationState.phoneNumber,
                            RegistrationState.name,
                            "driver"
                        )

                        if (res.isSuccess) {
                            // ✅ Navigate to Customer Dashboard (or Driver Dashboard if needed)
                            navController.navigate("customer_dashboard") {
                                popUpTo("register_screen") { inclusive = true }
                            }
                        } else {
                            println("Error: ${res.exceptionOrNull()?.message}")
                        }
                    }
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
                    text = "DRIVER",
                    fontFamily = PoppinsLight,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }