package com.example.goukm.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goukm.ui.userprofile.UserProfile
import com.example.goukm.ui.userprofile.UserProfileRepository
import com.example.goukm.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Define the authentication status
sealed class AuthState {
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

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser

    // 2. Initializer to check session status on startup
    init {
        checkSession()
    }

    // Function to check saved session
    fun checkSession() {
        viewModelScope.launch {
            if (sessionManager.fetchAuthToken() != null) {
                _authState.value = AuthState.LoggedIn
                fetchUserProfile()
            } else {
                _authState.value = AuthState.LoggedOut
            }
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            val user = UserProfileRepository.getUserProfile()
            _currentUser.value = user
        }
    }

    fun updateUserProfile(updatedUser: UserProfile) {
        viewModelScope.launch {
            val success = UserProfileRepository.updateUserProfile(updatedUser)
            if (success) {
                _currentUser.value = updatedUser
            }
        }
    }

    fun clearUser() {
        _currentUser.value = null
    }

    // FUNGSI BAHARU: Kemas kini peranan pengguna
    // Dipanggil dari DriverApplicationFormScreen.kt
    suspend fun updateUserRole(newRole: String): Boolean {
        val success = when (newRole) {
            "driver" -> UserProfileRepository.updateDriverRoleTrue()
            "customer" -> UserProfileRepository.updateCustomerRoleTrue()
            else -> false
        }

        if (success) {
            _currentUser.value?.let { user ->
                _currentUser.value = when (newRole) {
                    "driver" -> user.copy(role_driver = true)   // ✅ CUSTOMER → DRIVER
                    "customer" -> user.copy(role_customer = true)
                    else -> user
                }
            }
        } else {
            println("Error: Failed to update role in Firestore.")
        }

        return success
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
            fetchUserProfile()
        }
    }

    // 3. Function to clear session (used by CustomerProfileScreen)
    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            clearUser()
            _authState.value = AuthState.LoggedOut // 👈 Update State
        }
    }
}