package com.example.goukm.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goukm.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow // 👈 NEW
import kotlinx.coroutines.flow.StateFlow // 👈 NEW
import kotlinx.coroutines.launch

// Define the authentication status
sealed class AuthState { // 👈 NEW
    object Loading : AuthState()
    object LoggedIn : AuthState()
    object LoggedOut : AuthState()
}

// Renamed from LoginViewModel to AuthViewModel
class AuthViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    // 1. State Flow to hold the current authentication status
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    // 2. Initializer to check session status on startup
    init {
        checkSession()
    }

    // Function to check saved session
    fun checkSession() {
        viewModelScope.launch {
            if (sessionManager.fetchAuthToken() != null) {
                _authState.value = AuthState.LoggedIn
            } else {
                _authState.value = AuthState.LoggedOut
            }
        }
    }

    // Renamed from handleLoginSuccess, used by LoginScreen
    fun handleLogin(
        matricNumber: String,
        password: String,
        onLoginSuccess: (token: String) -> Unit
    ) {
        viewModelScope.launch {
            // ... (Your conceptual API call and validation logic here) ...

            // On conceptual success:
            if (matricNumber.isNotBlank() && password.isNotBlank()) {
                val authToken = "jwt_token_${matricNumber}_${System.currentTimeMillis()}"
                handleLoginSuccess(authToken) // Use the new function
                onLoginSuccess(authToken)
            } else {
                // Handle failure
            }
        }
    }

    fun handleLoginSuccess(token: String) {
        viewModelScope.launch {
            // 1. Save the token (the UID from Firebase or similar)
            sessionManager.saveAuthToken(token)

            // 2. Update the state to LoggedIn
            _authState.value = AuthState.LoggedIn
        }
    }

    // 3. Function to clear session (used by CustomerProfileScreen)
    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _authState.value = AuthState.LoggedOut // 👈 Update State
        }
    }
}