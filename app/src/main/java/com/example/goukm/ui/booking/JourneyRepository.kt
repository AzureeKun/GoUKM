package com.example.goukm.ui.booking

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

data class Journey(
    val id: String = "",
    val bookingId: String = "",
    val userId: String = "",
    val driverId: String = "",
    val pickup: String = "",
    val dropOff: String = "",
    val pickupLat: Double = 0.0,
    val pickupLng: Double = 0.0,
    val dropOffLat: Double = 0.0,
    val dropOffLng: Double = 0.0,
    val offeredFare: String = "",
    val seatType: String = "",
    val timestamp: Date = Date(), // Completion time
    val paymentMethod: String = "",
    val paymentStatus: String = "PAID",
    val rating: Float = 0f,
    val comment: String = ""
)

object JourneyRepository {
    private val db = FirebaseFirestore.getInstance()
    private val journeysCollection = db.collection("journeys")

    suspend fun createJourney(journey: Journey): Result<Unit> {
        return try {
            journeysCollection.document(journey.id).set(journey).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateJourneyRating(journeyId: String, rating: Float, comment: String): Result<Unit> {
        return try {
            journeysCollection.document(journeyId).update(
                mapOf(
                    "rating" to rating,
                    "comment" to comment
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createJourneyFromBooking(booking: Booking): Result<Unit> {
        val journey = Journey(
            id = booking.id,
            bookingId = booking.id,
            userId = booking.userId,
            driverId = booking.driverId,
            pickup = booking.pickup,
            dropOff = booking.dropOff,
            pickupLat = booking.pickupLat,
            pickupLng = booking.pickupLng,
            dropOffLat = booking.dropOffLat,
            dropOffLng = booking.dropOffLng,
            offeredFare = booking.offeredFare,
            seatType = booking.seatType,
            timestamp = Date(),
            paymentMethod = booking.paymentMethod,
            paymentStatus = booking.paymentStatus
        )
        return createJourney(journey)
    }

    fun getJourneysByDriver(driverId: String): Flow<List<Journey>> = callbackFlow {
        val listener = journeysCollection
            .whereEqualTo("driverId", driverId)
            .limit(1000) // Safety limit for memory
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val journeys = snapshot?.documents?.mapNotNull { it.toObject(Journey::class.java) } ?: emptyList()
                trySend(journeys)
            }
        awaitClose { listener.remove() }
    }

    fun getJourneysByUser(userId: String): Flow<List<Journey>> = callbackFlow {
        val listener = journeysCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val journeys = snapshot?.documents?.mapNotNull { it.toObject(Journey::class.java) } ?: emptyList()
                trySend(journeys)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getJourney(journeyId: String): Result<Journey> {
        return try {
            val doc = journeysCollection.document(journeyId).get().await()
            if (doc.exists()) {
                val journey = doc.toObject(Journey::class.java)
                if (journey != null) Result.success(journey)
                else Result.failure(Exception("Failed to parse journey"))
            } else {
                Result.failure(Exception("Journey not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
