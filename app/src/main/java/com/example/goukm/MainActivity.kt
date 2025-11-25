package com.example.goukm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.goukm.ui.theme.GoUKMTheme
import com.example.goukm.ui.register.RegisterScreen
import com.example.goukm.ui.userprofile.CustomerProfileScreen
import com.example.goukm.ui.userprofile.UserProfile

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoUKMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // 1. Define the user object with placeholder data
                    val currentUser = UserProfile(
                        name = "Ahmad Bin Abu",
                        matricNumber = "A18CS0123"
                    )

                    // 2. Call the Profile Screen, correctly passing the 'user' parameter
                    CustomerProfileScreen(
                        user = currentUser,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}