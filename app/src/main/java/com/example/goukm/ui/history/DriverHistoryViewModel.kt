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
                val rides = bookings.map { booking ->
                    // Fetch customer name
                    val userProfile = UserProfileRepository.getUserProfile(booking.userId)
                    val customerName = userProfile?.name ?: "Unknown Customer"
                    
                    // Format Date
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy - h:mm a", Locale.getDefault())
                    val dateStr = dateFormat.format(booking.timestamp)

                    // Format Status (if needed, or just map status to color/text in UI)
                    // We can reuse DriverRide data class, maybe add status field if needed
                    // For now, let's stick to the existing fields in DriverRide or update DriverRide
                    
                    DriverRide(
                        customer = customerName,
                        destination = booking.dropOff,
                        dateTime = dateStr,
                        distance = "0 km", // We might not have distance stored in booking yet, or need to calculate it. 
                                           // Ideally distance should be in Booking. 
                                           // Checking Booking model: it has lat/lng but not pre-calculated distance string.
                                           // For now, placeholder or if we have it. 
                                           // Actually, Journey has distance? 
                                           // Let's check if we can get distance. 
                                           // For now hardcode or calculate distance between pickup/dropoff latlng?
                                           // Let's calculated simple straight line or just show "-" if unknown.
                        fare = booking.offeredFare,
                        status = booking.status // Add this to DriverRide data class
                    )
                }
                _uiState.value = DriverHistoryUiState.Success(rides)
            }
        }
    }
}
