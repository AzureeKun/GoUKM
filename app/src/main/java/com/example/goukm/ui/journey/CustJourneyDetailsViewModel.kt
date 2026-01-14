package com.example.goukm.ui.journey

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.goukm.ui.booking.BookingRepository
import com.example.goukm.ui.booking.PlacesRepository
import com.example.goukm.ui.booking.RatingRepository
import com.example.goukm.ui.chat.ChatRepository
import com.example.goukm.ui.userprofile.UserProfileRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class CustJourneyDetailsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val bookingRepository = BookingRepository()
    private val placesRepository = PlacesRepository(application)
    private val db = FirebaseFirestore.getInstance()

    // State
    private val _paymentStatus = MutableStateFlow("PENDING")
    val paymentStatus: StateFlow<String> = _paymentStatus.asStateFlow()

    private val _driverArrived = MutableStateFlow(false)
    val driverArrived: StateFlow<Boolean> = _driverArrived.asStateFlow()

    private val _tripStatus = MutableStateFlow("")
    val tripStatus: StateFlow<String> = _tripStatus.asStateFlow()

    private val _arrivedAtDropOff = MutableStateFlow(false)
    val arrivedAtDropOff: StateFlow<Boolean> = _arrivedAtDropOff.asStateFlow()
    
    private val _driverLatLng = MutableStateFlow<LatLng?>(null)
    val driverLatLng: StateFlow<LatLng?> = _driverLatLng.asStateFlow()

    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints: StateFlow<List<LatLng>> = _routePoints.asStateFlow()
    
    // Details
    private val _driverName = MutableStateFlow("Driver")
    val driverName: StateFlow<String> = _driverName.asStateFlow()
    
    private val _driverPhone = MutableStateFlow("")
    val driverPhone: StateFlow<String> = _driverPhone.asStateFlow()
    
    private val _chatRoomId = MutableStateFlow("")
    val chatRoomId: StateFlow<String> = _chatRoomId.asStateFlow()
    
    private val _carBrand = MutableStateFlow("Brand")
    val carBrand: StateFlow<String> = _carBrand.asStateFlow()

    private val _carModel = MutableStateFlow("Model")
    val carModel: StateFlow<String> = _carModel.asStateFlow()

    private val _carColor = MutableStateFlow("Color")
    val carColor: StateFlow<String> = _carColor.asStateFlow()
    
    private val _carPlate = MutableStateFlow("Plate")
    val carPlate: StateFlow<String> = _carPlate.asStateFlow()

    private val _driverProfileUrl = MutableStateFlow("")
    val driverProfileUrl: StateFlow<String> = _driverProfileUrl.asStateFlow()

    private val _pickupAddress = MutableStateFlow("Loading...")
    val pickupAddress: StateFlow<String> = _pickupAddress.asStateFlow()
    
    private val _dropOffAddress = MutableStateFlow("Loading...")
    val dropOffAddress: StateFlow<String> = _dropOffAddress.asStateFlow()
    
    private val _fareAmount = MutableStateFlow("...")
    val fareAmount: StateFlow<String> = _fareAmount.asStateFlow()
    
    private val _passengerName = MutableStateFlow("Passenger")
    val passengerName: StateFlow<String> = _passengerName.asStateFlow()
    
    private val _passengerProfileUrl = MutableStateFlow("")
    val passengerProfileUrl: StateFlow<String> = _passengerProfileUrl.asStateFlow()
    
    private val _pickupLatLng = MutableStateFlow<LatLng?>(null)
    val pickupLatLng: StateFlow<LatLng?> = _pickupLatLng.asStateFlow()
    
    private val _dropOffLatLng = MutableStateFlow<LatLng?>(null)
    val dropOffLatLng: StateFlow<LatLng?> = _dropOffLatLng.asStateFlow()
    
    private val _driverRating = MutableStateFlow("New")
    val driverRating: StateFlow<String> = _driverRating.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Alerts
    private val _showArrivedAlert = MutableStateFlow(false)
    val showArrivedAlert: StateFlow<Boolean> = _showArrivedAlert.asStateFlow()
    
    private val _showDriverCancelledAlert = MutableStateFlow(false)
    val showDriverCancelledAlert: StateFlow<Boolean> = _showDriverCancelledAlert.asStateFlow()
    
    private val _navToRating = MutableStateFlow(false)
    val navToRating: StateFlow<Boolean> = _navToRating.asStateFlow()

    private var listener: ListenerRegistration? = null
    private var currentBookingId: String? = null

    fun initialize(bookingId: String, initialPaymentStatus: String) {
        if (currentBookingId == bookingId) return // Already initialized
        currentBookingId = bookingId
        driverDataDeepStarted = false
        _paymentStatus.value = initialPaymentStatus
        
        loadBookingData(bookingId)
        startListening(bookingId)
    }

    private fun loadBookingData(bookingId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = bookingRepository.getBooking(bookingId)
            val booking = result.getOrNull()
            if (booking != null) {
                _pickupAddress.value = booking.pickup
                _dropOffAddress.value = booking.dropOff
                _fareAmount.value = "RM ${booking.offeredFare}"
                _pickupLatLng.value = LatLng(booking.pickupLat, booking.pickupLng)
                _dropOffLatLng.value = LatLng(booking.dropOffLat, booking.dropOffLng)

                // INSTANT LOADING: Populate driver details from the booking record itself
                if (booking.driverId.isNotEmpty()) {
                    _driverName.value = booking.driverName
                    _driverPhone.value = booking.driverPhone
                    _carBrand.value = booking.driverCarBrand
                    _carModel.value = booking.driverVehicleType
                    _carColor.value = booking.driverCarColor
                    _carPlate.value = booking.driverVehiclePlateNumber
                    _driverProfileUrl.value = booking.driverProfileUrl
                    
                    // Background deep-fetch for dynamic data (rating, chatroom existence)
                    fetchDriverDataDeep(booking.driverId)
                }

                // Parallel Fetch for customer profile
                val customerProfileJob = async { UserProfileRepository.getUserProfile(booking.userId) }
                
                val customerProfile = customerProfileJob.await()
                if (customerProfile != null) {
                    _passengerName.value = customerProfile.name
                    _passengerProfileUrl.value = customerProfile.profilePictureUrl ?: ""
                }
            }
            _isLoading.value = false
        }
    }

    private var driverDataDeepStarted = false
    private fun fetchDriverDataDeep(driverId: String) {
        if (driverDataDeepStarted) return
        driverDataDeepStarted = true
        
        viewModelScope.launch {
            val bookingId = currentBookingId ?: return@launch
            
            // Background deep-fetches (Rating and ChatRoom)
            val statsJob = async { RatingRepository.getDriverStats(driverId) }
            val chatRoomJob = async { ChatRepository.getChatRoomByBookingId(bookingId) }
            
            val stats = statsJob.await()
            val chatRoomResult = chatRoomJob.await()
            
            // Update rating
            _driverRating.value = if (stats.totalReviews > 0) {
                String.format("%.1f", stats.averageRating)
            } else "New"
            
            // Confirm chatroom
            chatRoomResult.onSuccess { room ->
                if (room != null) _chatRoomId.value = room.id
            }
        }
    }

    private fun startListening(bookingId: String) {
        listener?.remove()
        
        listener = db.collection("bookings").document(bookingId).addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

            val status = snapshot.getString("status") ?: ""
            val pStatus = snapshot.getString("paymentStatus") ?: "PENDING"
            val arrived = snapshot.getBoolean("driverArrived") ?: false
            val arrivedDropOff = snapshot.getBoolean("arrivedAtDropOff") ?: false
            val dLat = snapshot.getDouble("currentDriverLat") ?: 0.0
            val dLng = snapshot.getDouble("currentDriverLng") ?: 0.0
            val dId = snapshot.getString("driverId") ?: ""

            _tripStatus.value = status
            _paymentStatus.value = pStatus 
            _driverArrived.value = arrived
            _arrivedAtDropOff.value = arrivedDropOff
            
            if (dId.isNotEmpty()) {
                fetchDriverDataDeep(dId)
            }
            
            if (dLat != 0.0 && dLng != 0.0) {
                val newLoc = LatLng(dLat, dLng)
                if (_driverLatLng.value != newLoc) {
                    _driverLatLng.value = newLoc
                    updateRouteToTarget(newLoc, arrived)
                }
            }

            if (status == "COMPLETED") {
                if (pStatus == "PAID") {
                    _navToRating.value = true
                }
            } else if (arrivedDropOff && pStatus != "PAID") {
                _showArrivedAlert.value = true
            } else if (status == com.example.goukm.ui.booking.BookingStatus.CANCELLED_BY_DRIVER.name) {
                _showDriverCancelledAlert.value = true
            }
        }
    }
    
    private fun updateRouteToTarget(driverLoc: LatLng, isArrived: Boolean) {
        val targetLoc = if (isArrived) _dropOffLatLng.value else _pickupLatLng.value
        if (targetLoc == null) return
        
        viewModelScope.launch {
             val result = placesRepository.getRoute(driverLoc, targetLoc)
             result.onSuccess {
                 _routePoints.value = it.polyline
             }
        }
    }
    
    fun dismissArrivedAlert() {
        // usually can't dismiss until paid, but if logic allows
    }
    
    fun dismissDriverCancelled() {
        _showDriverCancelledAlert.value = false
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
