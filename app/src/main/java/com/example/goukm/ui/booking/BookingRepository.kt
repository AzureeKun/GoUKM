package com.example.goukm.ui.booking

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

enum class BookingStatus {
    PENDING,
    OFFERED,
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
    val timestamp: Date = Date(),
    val offeredFare: String = "",
    val driverId: String = "",
    val pickupLat: Double = 0.0,
    val pickupLng: Double = 0.0,
    val dropOffLat: Double = 0.0,
    val dropOffLng: Double = 0.0
)

class BookingRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val bookingsCollection = firestore.collection("bookings")

    suspend fun createBooking(
        pickup: String, 
        dropOff: String, 
        seatType: String,
        pickupLat: Double,
        pickupLng: Double,
        dropOffLat: Double,
        dropOffLng: Double
    ): Result<String> {
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
                timestamp = Date(),
                offeredFare = "",
                driverId = "",
                pickupLat = pickupLat,
                pickupLng = pickupLng,
                dropOffLat = dropOffLat,
                dropOffLng = dropOffLng
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

    suspend fun updateFare(bookingId: String, fare: String, driverId: String): Result<Unit> {
        return try {
            bookingsCollection.document(bookingId).update(
                mapOf(
                    "offeredFare" to fare,
                    "driverId" to driverId
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBooking(bookingId: String): Result<Booking> {
        return try {
            val doc = bookingsCollection.document(bookingId).get().await()
            if (doc.exists()) {
                val booking = Booking(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    pickup = doc.getString("pickup") ?: "",
                    dropOff = doc.getString("dropOff") ?: "",
                    seatType = doc.getString("seatType") ?: "",
                    status = doc.getString("status") ?: "",
                    offeredFare = doc.getString("offeredFare") ?: "",
                    driverId = doc.getString("driverId") ?: "",
                    timestamp = doc.getDate("timestamp") ?: Date(),
                    pickupLat = doc.getDouble("pickupLat") ?: 0.0,
                    pickupLng = doc.getDouble("pickupLng") ?: 0.0,
                    dropOffLat = doc.getDouble("dropOffLat") ?: 0.0,
                    dropOffLng = doc.getDouble("dropOffLng") ?: 0.0
                )
                Result.success(booking)
            } else {
                Result.failure(Exception("Not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}