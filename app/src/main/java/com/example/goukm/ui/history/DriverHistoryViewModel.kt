package com.example.goukm.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goukm.ui.booking.BookingRepository
import com.example.goukm.ui.userprofile.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

sealed class DriverHistoryUiState {
    object Loading : DriverHistoryUiState()
    data class Success(val rides: List<DriverRide>) : DriverHistoryUiState()
    data class Error(val message: String) : DriverHistoryUiState()
}

class DriverHistoryViewModel : ViewModel() {
    private val bookingRepository = BookingRepository()
    // UserProfileRepository is an object, so we can access it directly

    private val _uiState = MutableStateFlow<DriverHistoryUiState>(DriverHistoryUiState.Loading)
    val uiState: StateFlow<DriverHistoryUiState> = _uiState.asStateFlow()

    private val nameCache = java.util.concurrent.ConcurrentHashMap<String, String>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy - h:mm a", Locale.getDefault())

    init {
        fetchHistory()
    }

    private fun fetchHistory() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            _uiState.value = DriverHistoryUiState.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            bookingRepository.getDriverHistory(currentUserId).collect { bookings ->
                // 1. Initial Mapping: Show everything immediately with available names or "Student" placeholder
                fun createRidesList() = bookings.map { booking ->
                    val customerName = when {
                        booking.userName.isNotEmpty() -> booking.userName
                        nameCache.containsKey(booking.userId) -> nameCache[booking.userId]!!
                        else -> "Student" // Faster placeholder than "Loading..."
                    }

                    DriverRide(
                        id = booking.id,
                        customer = customerName,
                        destination = booking.dropOff,
                        dateTime = dateFormat.format(booking.timestamp),
                        distance = "0 km",
                        fare = booking.offeredFare,
                        status = booking.status
                    )
                }

                // Show the list immediately! No spinner.
                _uiState.value = DriverHistoryUiState.Success(createRidesList())

                // 2. Background Fetching: Fetch missing names one by one and update UI as they arrive
                bookings.filter { it.userName.isEmpty() && !nameCache.containsKey(it.userId) }
                        .map { it.userId }
                        .distinct()
                        .forEach { uid ->
                            launch {
                                try {
                                    val profile = UserProfileRepository.getUserProfile(uid)
                                    val name = profile?.name ?: "Unknown student"
                                    nameCache[uid] = name
                                    // Re-emit the list with the newly found name
                                    _uiState.value = DriverHistoryUiState.Success(createRidesList())
                                } catch (e: Exception) {
                                    // Silent fail for background name fetch
                                }
                            }
                        }
            }
        }
    }
}
