package com.example.goukm.ui.booking

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

enum class BookingStatus {
    PENDING,
    ACCEPTED,
    COMPLETED,
    CANCELLED
}

data class Booking(
    val id: String = "",
    val userId: String = "",
    val pickup: String = "",
    val dropOff: String = "",
    val seatType: String = "",
    val status: String = BookingStatus.PENDING.name,
    val timestamp: Date = Date()
)

class BookingRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val bookingsCollection = firestore.collection("bookings")

    suspend fun createBooking(pickup: String, dropOff: String, seatType: String): Result<String> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        
        return try {
            val bookingId = bookingsCollection.document().id
            val booking = Booking(
                id = bookingId,
                userId = currentUser.uid,
                pickup = pickup,
                dropOff = dropOff,
                seatType = seatType,
                status = BookingStatus.PENDING.name,
                timestamp = Date()
            )
            
            bookingsCollection.document(bookingId).set(booking).await()
            Result.success(bookingId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStatus(bookingId: String, status: BookingStatus): Result<Unit> {
        return try {
            bookingsCollection.document(bookingId).update("status", status.name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
