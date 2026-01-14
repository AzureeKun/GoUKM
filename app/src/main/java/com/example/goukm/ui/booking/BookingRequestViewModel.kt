package com.example.goukm.ui.booking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.goukm.navigation.NavRoutes
import com.example.goukm.ui.userprofile.UserProfileRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BookingRequestViewModel(application: Application) : AndroidViewModel(application) {
    
    private val placesRepository = PlacesRepository(application)
    private val bookingRepository = BookingRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // State
    data class LocationInfo(
        val address: String = "",
        val latLng: LatLng? = null,
        val placeId: String? = null
    )

    private val _pickupLocation = MutableStateFlow(LocationInfo())
    val pickupLocation: StateFlow<LocationInfo> = _pickupLocation.asStateFlow()

    private val _dropOffLocation = MutableStateFlow(LocationInfo())
    val dropOffLocation: StateFlow<LocationInfo> = _dropOffLocation.asStateFlow()

    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints: StateFlow<List<LatLng>> = _routePoints.asStateFlow()

    private val _rideOffers = MutableStateFlow<List<DriverOffer>>(emptyList())
    val rideOffers: StateFlow<List<DriverOffer>> = _rideOffers.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _isCreatingBooking = MutableStateFlow(false)
    val isCreatingBooking: StateFlow<Boolean> = _isCreatingBooking.asStateFlow()

    private val _currentBookingId = MutableStateFlow<String?>(null)
    val currentBookingId: StateFlow<String?> = _currentBookingId.asStateFlow()

    private val _bookingStatus = MutableStateFlow("PENDING")
    val bookingStatus: StateFlow<String> = _bookingStatus.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow<PaymentMethod?>(null)
    val selectedPaymentMethod: StateFlow<PaymentMethod?> = _selectedPaymentMethod.asStateFlow()

    private val _pickupPredictions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val pickupPredictions: StateFlow<List<AutocompletePrediction>> = _pickupPredictions.asStateFlow()

    private val _dropOffPredictions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val dropOffPredictions: StateFlow<List<AutocompletePrediction>> = _dropOffPredictions.asStateFlow()

    // Nav Event
    private val _navToJourney = MutableStateFlow<String?>(null)
    val navToJourney: StateFlow<String?> = _navToJourney.asStateFlow()

    private var bookingListener: ListenerRegistration? = null
    private var offersListener: ListenerRegistration? = null

    init {
        // Restore active booking if any
        restoreActiveBooking()
        loadPreferredPaymentMethod()
    }

    private fun loadPreferredPaymentMethod() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val profile = UserProfileRepository.getUserProfile(uid)
            if (profile != null) {
                _selectedPaymentMethod.value = when (profile.preferredPaymentMethod) {
                    "QR_DUITNOW" -> PaymentMethod.QR_DUITNOW
                    else -> PaymentMethod.CASH
                }
            }
        }
    }

    private fun restoreActiveBooking() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            // Check if there is already an active booking locally or fetch from server
            db.collection("bookings")
                .whereEqualTo("userId", uid)
                .whereIn("status", listOf("PENDING", "OFFERED", "ACCEPTED", "ONGOING"))
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        val doc = snapshot.documents.first()
                        _currentBookingId.value = doc.id
                        _isSearching.value = true
                        startListening(doc.id)
                        
                        // Restore locations
                        _pickupLocation.value = LocationInfo(
                            address = doc.getString("pickup") ?: "",
                            latLng = LatLng(doc.getDouble("pickupLat") ?: 0.0, doc.getDouble("pickupLng") ?: 0.0),
                            placeId = "restored"
                        )
                         _dropOffLocation.value = LocationInfo(
                            address = doc.getString("dropOff") ?: "",
                            latLng = LatLng(doc.getDouble("dropOffLat") ?: 0.0, doc.getDouble("dropOffLng") ?: 0.0),
                            placeId = "restored"
                        )
                        updateRoute()
                    }
                }
        }
    }

    fun setBookingId(id: String) {
        if (_currentBookingId.value != id) {
            _currentBookingId.value = id
            startListening(id)
        }
    }

    private fun startListening(bookingId: String) {
        bookingListener?.remove()
        offersListener?.remove()

        val docRef = db.collection("bookings").document(bookingId)
        
        bookingListener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
            
            val status = snapshot.getString("status") ?: "PENDING"
            _bookingStatus.value = status

            if (status == "ACCEPTED" || status == "ONGOING") {
                 _navToJourney.value = bookingId
            }
        }

        offersListener = docRef.collection("offers").addSnapshotListener { offersSnapshot, offersError ->
            if (offersError != null || offersSnapshot == null) return@addSnapshotListener
            
            val offers = offersSnapshot.documents.mapNotNull { doc ->
                val f = doc.getString("fare") ?: ""
                DriverOffer(
                    name = doc.getString("driverName") ?: "",
                    fareLabel = "RM $f",
                    carBrand = doc.getString("carBrand") ?: "",
                    carName = doc.getString("vehicleType") ?: "",
                    carColor = doc.getString("carColor") ?: "",
                    plate = doc.getString("vehiclePlateNumber") ?: "",
                    driverId = doc.getString("driverId") ?: "",
                    driverPhone = doc.getString("phoneNumber") ?: "",
                    driverProfileUrl = doc.getString("driverProfileUrl") ?: ""
                )
            }
            _rideOffers.value = offers
        }
    }

    fun updatePickup(query: String) {
        _pickupLocation.value = _pickupLocation.value.copy(address = query, latLng = null, placeId = null)
        viewModelScope.launch {
            _pickupPredictions.value = placesRepository.getPredictions(query)
        }
    }

    fun selectPickup(placeId: String, address: String) {
         _pickupPredictions.value = emptyList()
         viewModelScope.launch {
             val place = placesRepository.getPlaceDetails(placeId)
             _pickupLocation.value = LocationInfo(address = address, latLng = place?.latLng, placeId = placeId)
             updateRoute()
         }
    }

    fun selectPickupFromRecent(place: RecentPlace) {
        _pickupLocation.value = LocationInfo(address = place.address, latLng = LatLng(place.lat, place.lng), placeId = "recent")
        updateRoute()
    }

    fun setPickupLatLng(latLng: LatLng, address: String) {
        _pickupLocation.value = LocationInfo(address = address, latLng = latLng, placeId = "map")
        updateRoute()
    }

    fun updateDropOff(query: String) {
        _dropOffLocation.value = _dropOffLocation.value.copy(address = query, latLng = null, placeId = null)
        viewModelScope.launch {
            _dropOffPredictions.value = placesRepository.getPredictions(query, _pickupLocation.value.latLng)
        }
    }

    fun selectDropOff(placeId: String, address: String) {
        _dropOffPredictions.value = emptyList()
        viewModelScope.launch {
            val place = placesRepository.getPlaceDetails(placeId)
            _dropOffLocation.value = LocationInfo(address = address, latLng = place?.latLng, placeId = placeId)
            updateRoute()
        }
    }
    
    fun selectDropOffFromRecent(place: RecentPlace) {
        _dropOffLocation.value = LocationInfo(address = place.address, latLng = LatLng(place.lat, place.lng), placeId = "recent")
        updateRoute()
    }

    fun setDropOffLocation(latLng: LatLng, address: String, placeId: String) {
        _dropOffLocation.value = LocationInfo(address = address, latLng = latLng, placeId = placeId)
        updateRoute()
    }

    private fun updateRoute() {
        val start = _pickupLocation.value.latLng
        val end = _dropOffLocation.value.latLng
        if (start != null && end != null) {
            viewModelScope.launch {
                val result = placesRepository.getRoute(start, end)
                result.onSuccess {
                    _routePoints.value = it.polyline
                }.onFailure {
                    _routePoints.value = listOf(start, end)
                }
            }
        }
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
        viewModelScope.launch {
             UserProfileRepository.updatePreferredPaymentMethod(method.name)
        }
    }

    fun createBooking(seatType: String, onError: (String) -> Unit) {
        val pickup = _pickupLocation.value
        val dropOff = _dropOffLocation.value
        val payment = _selectedPaymentMethod.value

        if (pickup.latLng == null || dropOff.latLng == null || payment == null) return
        if (_isCreatingBooking.value) return

        viewModelScope.launch {
            _isCreatingBooking.value = true
            val result = bookingRepository.createBooking(
                pickup = pickup.address,
                dropOff = dropOff.address,
                seatType = seatType,
                pickupLat = pickup.latLng.latitude,
                pickupLng = pickup.latLng.longitude,
                dropOffLat = dropOff.latLng.latitude,
                dropOffLng = dropOff.latLng.longitude,
                paymentMethod = payment.name
            )
            
            result.onSuccess { id ->
                _currentBookingId.value = id
                _isSearching.value = true
                startListening(id)
            }.onFailure {
                onError(it.message ?: "Failed to create booking")
            }
            _isCreatingBooking.value = false
        }
    }
    
    fun acceptOffer(offer: DriverOffer, context: android.content.Context) {
        val bookingId = _currentBookingId.value ?: return
        viewModelScope.launch {
             try {
                // Update booking with the accepted offer details
                val acceptedOffer = com.example.goukm.ui.booking.Offer(
                    driverId = offer.driverId,
                    driverName = offer.name,
                    carBrand = offer.carBrand,
                    carColor = offer.carColor,
                    vehicleType = offer.carName,
                    vehiclePlateNumber = offer.plate,
                    phoneNumber = offer.driverPhone,
                    driverProfileUrl = offer.driverProfileUrl,
                    fare = offer.fareLabel.replace("RM ", "")
                )
                
                // Create Chat Room
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val customerProfile = UserProfileRepository.getUserProfile(currentUser.uid)
                    com.example.goukm.ui.chat.ChatRepository.createChatRoom(
                        bookingId = bookingId,
                        customerId = currentUser.uid,
                        driverId = offer.driverId,
                        customerName = customerProfile?.name ?: "Customer",
                        driverName = offer.name,
                        customerPhone = customerProfile?.phoneNumber ?: "",
                        driverPhone = offer.driverPhone
                    )
                }

                // Update Booking Status
                bookingRepository.acceptOffer(bookingId, acceptedOffer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cancelSearch() {
        cancelBooking()
    }

    fun cancelBooking() {
        val id = _currentBookingId.value ?: return
        viewModelScope.launch {
            bookingRepository.updateStatus(id, BookingStatus.CANCELLED_BY_CUSTOMER)
            _isSearching.value = false
            _currentBookingId.value = null
            _rideOffers.value = emptyList()
            stopListening()
        }
    }
    
    private fun stopListening() {
        bookingListener?.remove()
        offersListener?.remove()
        bookingListener = null
        offersListener = null
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
    
    fun onNavigatedToJourney() {
        _navToJourney.value = null
    }
}
