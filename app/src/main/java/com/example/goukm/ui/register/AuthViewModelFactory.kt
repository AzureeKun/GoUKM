package com.example.goukm.ui.register

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.goukm.util.SessionManager

// Renamed from LoginViewModelFactory to AuthViewModelFactory
class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) { // 👈 Renamed here

            val sessionManager = SessionManager(context.applicationContext)

            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(sessionManager) as T // 👈 Renamed here
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}