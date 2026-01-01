package com.example.goukm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.goukm.navigation.AppNavGraph
import com.example.goukm.ui.theme.GoUKMTheme
import com.example.goukm.ui.register.RegisterScreen

        enableEdgeToEdge()

        // Get and Save FCM Token
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                android.util.Log.d("FCM_DEBUG", "FCM Token: $token")
                
                // Save token to current user profile
                if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null) {
                    androidx.lifecycle.lifecycleScope.launch {
                        com.example.goukm.ui.userprofile.UserProfileRepository.saveFCMToken(token)
                    }
                }
            }
        }

        setContent {
            GoUKMTheme {
                val navController = rememberNavController()
                AppNavGraph(navController)
            }
        }
    }
}