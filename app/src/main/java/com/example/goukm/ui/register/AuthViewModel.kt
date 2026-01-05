package com.example.goukm.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goukm.ui.userprofile.UserProfile
import com.example.goukm.ui.userprofile.UserProfileRepository
import com.example.goukm.util.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    private val _driverApplicationStatus = MutableStateFlow<String?>(null)
    val driverApplicationStatus: StateFlow<String?> = _driverApplicationStatus

    private var applicationListener: ListenerRegistration? = null
    
    // Flag to handle app startup state reset
    private var isFirstLoad = true


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

            // Use UserProfileRepository for proper field mapping
            val user = UserProfileRepository.getUserProfile()

            
            // Force Driver Offline on App Start (First Load)
            var finalUser = user
            if (isFirstLoad && user != null && user.role_driver == true && user.isAvailable) {
                finalUser = user.copy(isAvailable = false)
                launch { UserProfileRepository.updateDriverAvailability(false) }
                isFirstLoad = false
            } else {
                 // For subsequent fetches, respect the fetched state (or if not driver/available)
                 if (isFirstLoad) isFirstLoad = false
            }

            _currentUser.value = finalUser

            // Restore from Firestore or default to customer
            _activeRole.value = if (defaultToCustomer) {
                "customer"
            } else {
                sessionManager.fetchActiveRole() ?: "customer"
            }

            // Sync FCM Token
            // Sync FCM Token
            try {
                val token = com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
                android.util.Log.d("FCM_DEBUG", "Saving Token in ViewModel: $token")
                UserProfileRepository.saveFCMToken(token)
            } catch (e: Exception) {
                android.util.Log.e("FCM_DEBUG", "Failed to get token in ViewModel", e)
            }

            listenToUserDocument(uid)
            listenToDriverApplication(uid)

            _authState.value = AuthState.LoggedIn
        }
    }

    private var userListener: ListenerRegistration? = null

    private fun listenToUserDocument(uid: String) {
        userListener?.remove()
        userListener = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val user = snapshot?.toObject(UserProfile::class.java)
                if (user != null) {
                    val oldRoleDriver = _currentUser.value?.role_driver
                    _currentUser.value = user
                    
                    // If role_driver changed from false to true, we might want to react
                    if (oldRoleDriver == false && user.role_driver == true) {
                        // User was just approved as driver
                        _activeRole.value = "driver"
                        viewModelScope.launch {
                            sessionManager.saveActiveRole("driver")
                        }
                    }
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
            // Remove FCM Token from current user to prevent notifications on this device
            try {
                UserProfileRepository.saveFCMToken("") // Saving empty string deletes/clears it effectively
            } catch (e: Exception) {
                e.printStackTrace()
            }

            auth.signOut()                         // ✅ sign out firebase
            sessionManager.clearSession()         // ✅ clear token
            sessionManager.saveActiveRole("customer") // ✅ RESET TO CUSTOMER
            clearUser()
            _driverApplicationStatus.value = null
            applicationListener?.remove()
            userListener?.remove()
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
        // This is now handled by DriverApplicationViewModel and manual review.
        // We no longer update the user role directly here.
        onResult(true) 
    }

    private fun listenToDriverApplication(uid: String) {
        applicationListener?.remove()
        applicationListener = FirebaseFirestore.getInstance()
            .collection("driverApplications")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val status = snapshot?.getString("status")
                _driverApplicationStatus.value = status

                // Approval is handled via listenToUserDocument reacting to role_driver = true
            }
    }

    // Set driver availability locally and in Firestore
    fun setDriverAvailability(isAvailable: Boolean) {
        viewModelScope.launch {
            // Optimistic update locally
            _currentUser.value = _currentUser.value?.copy(isAvailable = isAvailable)
            
            // Update Firestore
            UserProfileRepository.updateDriverAvailability(isAvailable)
            
            if (isAvailable) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    UserProfileRepository.recordOnlineDay(uid)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        applicationListener?.remove()
        userListener?.remove()
    }
}