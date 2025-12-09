package com.example.goukm.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goukm.ui.userprofile.UserProfile
import com.example.goukm.ui.userprofile.UserProfileRepository
import com.example.goukm.util.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    private val auth = FirebaseAuth.getInstance()

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

            if (token != null) {
                _authState.value = AuthState.LoggedIn

                // Restore saved role from SessionManager, default to "customer" if not found
                val savedRole = sessionManager.fetchActiveRole() ?: "customer"
                _activeRole.value = savedRole

                // Fetch user profile without forcing customer mode, so it respects the saved role
                fetchUserProfile(defaultToCustomer = false)
            } else {
                _authState.value = AuthState.LoggedOut
            }
        }
    }


    fun fetchUserProfile(defaultToCustomer: Boolean = false) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch

            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()

            val user = doc.toObject(UserProfile::class.java)
            _currentUser.value = user

            // Restore from Firestore or default to customer
            _activeRole.value = if (defaultToCustomer) {
                "customer"
            } else {
                sessionManager.fetchActiveRole() ?: "customer"
            }

            _authState.value = AuthState.LoggedIn
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
                    "driver" -> user.copy(role_driver = true)
                    "customer" -> user.copy(role_customer = true)
                    else -> user
                }

                //_currentUser.value = updatedUser
                _currentUser.value = updatedUser
                _activeRole.value = newRole
                sessionManager.saveActiveRole(newRole)
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
        updateUserRole(newRole) // updates Firestore
        sessionManager.saveActiveRole(newRole) // ✅ persist to disk
        _activeRole.value = newRole // ✅ update in-memory
    }


    // 3. Function to clear session (used by CustomerProfileScreen)
    fun logout() {
        viewModelScope.launch {
            auth.signOut()                         // ✅ sign out firebase
            sessionManager.clearSession()         // ✅ clear token
            sessionManager.saveActiveRole("customer") // ✅ RESET TO CUSTOMER
            clearUser()
            _authState.value = AuthState.LoggedOut
        }
    }


    // Submit Driver Application with vehicle details
    fun submitDriverApplication(
        licenseNumber: String,
        vehiclePlateNumber: String,
        vehicleType: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user == null) {
                onResult(false)
                return@launch
            }

            val updatedUser = user.copy(
                role_driver = true, // Grant driver role
                licenseNumber = licenseNumber,
                vehiclePlateNumber = vehiclePlateNumber,
                vehicleType = vehicleType
            )

            val success = UserProfileRepository.updateUserProfile(updatedUser)
            if (success) {
                _currentUser.value = updatedUser
                _activeRole.value = "driver" // Switch to driver mode immediately
                sessionManager.saveActiveRole("driver")
            }
            onResult(success)
        }
    }

}