package com.example.goukm.ui.booking

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

enum class BookingStatus {
    PENDING,
    OFFERED,
    ACCEPTED,
    ONGOING,
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
    val dropOffLng: Double = 0.0,
    val driverArrived: Boolean = false,
    val paymentMethod: String = "CASH", // Default to CASH
    val paymentStatus: String = "PENDING",
    val offeredDriverIds: List<String> = emptyList(),
    val currentDriverLat: Double = 0.0,
    val currentDriverLng: Double = 0.0
)

data class Offer(
    val id: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val vehicleType: String = "",
    val vehiclePlateNumber: String = "",
    val phoneNumber: String = "",
    val fare: String = "",
    val timestamp: Date = Date()
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
        dropOffLng: Double,
        paymentMethod: String
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
                dropOffLng = dropOffLng,
                driverArrived = false,
                paymentMethod = paymentMethod
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
            
            if (status == BookingStatus.COMPLETED) {
                // Fetch the full booking to create a Journey
                val bookingResult = getBooking(bookingId)
                bookingResult.onSuccess { booking ->
                    JourneyRepository.createJourneyFromBooking(booking)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitOffer(bookingId: String, fare: String, driverId: String, driverName: String, vehicleType: String, vehiclePlateNumber: String, phoneNumber: String): Result<Unit> {
        return try {
            val offerId = bookingsCollection.document(bookingId).collection("offers").document().id
            val offer = Offer(
                id = offerId,
                driverId = driverId,
                driverName = driverName,
                vehicleType = vehicleType,
                vehiclePlateNumber = vehiclePlateNumber,
                phoneNumber = phoneNumber,
                fare = fare,
                timestamp = Date()
            )
            
            bookingsCollection.document(bookingId).collection("offers").document(offerId).set(offer).await()
            // Also update the main document status to OFFERED and add driver to the tracking list
            bookingsCollection.document(bookingId).update(
                mapOf(
                    "status" to BookingStatus.OFFERED.name,
                    "offeredDriverIds" to com.google.firebase.firestore.FieldValue.arrayUnion(driverId)
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptOffer(bookingId: String, offer: Offer): Result<Unit> {
        return try {
            bookingsCollection.document(bookingId).update(
                mapOf(
                    "status" to BookingStatus.ACCEPTED.name,
                    "driverId" to offer.driverId,
                    "offeredFare" to offer.fare
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    suspend fun updateDriverArrived(bookingId: String): Result<Unit> {
        return try {
            bookingsCollection.document(bookingId).update("driverArrived", true).await()
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
                    dropOffLng = doc.getDouble("dropOffLng") ?: 0.0,
                    driverArrived = doc.getBoolean("driverArrived") ?: false,
                    paymentMethod = doc.getString("paymentMethod") ?: "CASH",
                    paymentStatus = doc.getString("paymentStatus") ?: "PENDING",
                    offeredDriverIds = doc.get("offeredDriverIds") as? List<String> ?: emptyList()
                )
                Result.success(booking)
            } else {
                Result.failure(Exception("Not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePaymentStatus(bookingId: String, status: String): Result<Unit> {
        return try {
            bookingsCollection.document(bookingId).update("paymentStatus", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBookingsByDriverAndStatus(driverId: String, status: BookingStatus): Flow<List<Booking>> = callbackFlow {
        val listener = bookingsCollection
            .whereEqualTo("driverId", driverId)
            .whereEqualTo("status", status.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val bookings = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Booking(
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
                            dropOffLng = doc.getDouble("dropOffLng") ?: 0.0,
                            driverArrived = doc.getBoolean("driverArrived") ?: false,
                            paymentMethod = doc.getString("paymentMethod") ?: "CASH",
                            paymentStatus = doc.getString("paymentStatus") ?: "PENDING",
                            offeredDriverIds = doc.get("offeredDriverIds") as? List<String> ?: emptyList()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(bookings)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateDriverLocation(bookingId: String, lat: Double, lng: Double): Result<Unit> {
        return try {
            bookingsCollection.document(bookingId).update(
                mapOf(
                    "currentDriverLat" to lat,
                    "currentDriverLng" to lng
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}