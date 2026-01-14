package com.example.goukm.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goukm.ui.booking.Booking
import com.example.goukm.ui.booking.RecentPlace
import com.example.goukm.ui.booking.RecentPlaceRepository
import com.example.goukm.ui.chat.ChatRepository
import com.example.goukm.ui.chat.ChatRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class CustomerDashViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _activeBooking = MutableStateFlow<Booking?>(null)
    val activeBooking: StateFlow<Booking?> = _activeBooking.asStateFlow()

    private val _chatRoom = MutableStateFlow<ChatRoom?>(null)
    val chatRoom: StateFlow<ChatRoom?> = _chatRoom.asStateFlow()

    private val _recentPlaces = MutableStateFlow<List<RecentPlace>>(emptyList())
    val recentPlaces: StateFlow<List<RecentPlace>> = _recentPlaces.asStateFlow()

    private var bookingListener: ListenerRegistration? = null
    
    val greeting: String
        get() {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when {
                hour < 12 -> "Good Morning"
                hour < 17 -> "Good Afternoon"
                else -> "Good Evening"
            }
        }

    init {
        startListeningForActiveBooking()
        fetchRecentPlaces()
    }

    private fun startListeningForActiveBooking() {
        val currentUser = auth.currentUser ?: return

        // Remove existing listener if any
        bookingListener?.remove()

        bookingListener = db.collection("bookings")
            .whereEqualTo("userId", currentUser.uid)
            .whereIn("status", listOf("PENDING", "OFFERED", "ACCEPTED", "ONGOING"))
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || snapshot.isEmpty) {
                    _activeBooking.value = null
                    _chatRoom.value = null
                    return@addSnapshotListener
                }

                val doc = snapshot.documents.firstOrNull()
                if (doc != null) {
                    val booking = Booking(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        pickup = doc.getString("pickup") ?: "",
                        dropOff = doc.getString("dropOff") ?: "",
                        seatType = doc.getString("seatType") ?: "",
                        status = doc.getString("status") ?: "",
                        offeredFare = doc.getString("offeredFare") ?: "",
                        driverId = doc.getString("driverId") ?: "",
                        driverArrived = doc.getBoolean("driverArrived") ?: false,
                        paymentMethod = doc.getString("paymentMethod") ?: "CASH",
                        paymentStatus = doc.getString("paymentStatus") ?: "PENDING"
                    )
                    _activeBooking.value = booking

                    // If status allows chat, fetch chat room
                    if (booking.status == "ACCEPTED" || booking.status == "ONGOING") {
                         fetchChatRoom(booking.id)
                    } else {
                        _chatRoom.value = null
                    }
                } else {
                    _activeBooking.value = null
                    _chatRoom.value = null
                }
            }
    }

    private fun fetchChatRoom(bookingId: String) {
        viewModelScope.launch {
            val result = ChatRepository.getChatRoomByBookingId(bookingId)
            _chatRoom.value = result.getOrNull()
        }
    }

    fun fetchRecentPlaces() {
        viewModelScope.launch {
            _recentPlaces.value = RecentPlaceRepository.getRecentPlaces()
        }
    }

    override fun onCleared() {
        super.onCleared()
        bookingListener?.remove()
    }
}
