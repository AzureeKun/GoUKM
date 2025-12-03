package com.example.goukm.ui.register

import android.net.Uri
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

    private val _activeRole = MutableStateFlow("customer") // default customer
    val activeRole: StateFlow<String> = _activeRole


    // 2. Initializer to check session status on startup
    init {
        checkSession()
    }

    // Check if user session exists
    fun checkSession() {
        viewModelScope.launch {
            val token = sessionManager.fetchAuthToken()
            val role = sessionManager.fetchActiveRole() ?: "customer" // get last active role
            if (token != null) {
                _authState.value = AuthState.LoggedIn
                _activeRole.value = role // default customer if missing
                fetchUserProfile()
            } else {
                _authState.value = AuthState.LoggedOut
            }
        }
    }


    fun fetchUserProfile(defaultToCustomer: Boolean = false) {
        viewModelScope.launch {
            val user = UserProfileRepository.getUserProfile()
            _currentUser.value = user
            user?.let {
                _activeRole.value = if (defaultToCustomer) "customer"
                else if (it.role_driver) "driver"
                else "customer"
                sessionManager.saveActiveRole(_activeRole.value)
            }
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
        _activeRole.value = "customer"
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

                val updatedUser = when (newRole) {
                    "driver" -> user.copy(role_driver = true, role_customer = false)
                    "customer" -> user.copy(role_driver = false, role_customer = true)
                    else -> user
                }

                //_currentUser.value = updatedUser
                sessionManager.saveActiveRole(newRole)
                _activeRole.value = newRole
            }
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
                handleLoginSuccess(authToken)
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
            fetchUserProfile() // fetch profile termasuk gambar Storage
        }
    }

    //SWITCH ACCOUNT
    suspend fun switchActiveRole(newRole: String) {
        updateUserRole(newRole) // this already persists in SessionManager
    }


    // 3. Function to clear session (used by CustomerProfileScreen)
    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()          // clear token, etc.
            clearUser()                            // clear currentUser
            _activeRole.value = "customer"         // ✅ reset role to customer
            sessionManager.saveActiveRole("customer") // persist default role
            _authState.value = AuthState.LoggedOut
        }
    }

}