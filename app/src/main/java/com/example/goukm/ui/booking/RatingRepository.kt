package com.example.goukm.ui.booking

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.PropertyName

data class Rating(
    val id: String = "",
    val bookingId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val driverId: String = "",
    val pickup: String = "",
    val dropOff: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val timestamp: Long = 0
)

object RatingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val ratingsCollection = db.collection("ratings")
    private val bookingsCollection = db.collection("bookings")

    suspend fun submitRating(rating: Rating): Result<Unit> {
        return try {
            val docRef = ratingsCollection.document()
            val finalRating = rating.copy(id = docRef.id, timestamp = System.currentTimeMillis())
            docRef.set(finalRating).await()
            
            // Also update the Journey record if it exists
            JourneyRepository.updateJourneyRating(rating.bookingId, rating.rating, rating.comment)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRatingsForDriver(driverId: String): Result<List<Rating>> {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("driverId", driverId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val ratings = snapshot.toObjects(Rating::class.java)
            Result.success(ratings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDriverStats(driverId: String): DriverStats {
        return try {
            val ratingsSnapshot = ratingsCollection
                .whereEqualTo("driverId", driverId)
                .get()
                .await()
            
            val ratings = ratingsSnapshot.toObjects(Rating::class.java)
            val averageRating = if (ratings.isNotEmpty()) {
                ratings.map { it.rating }.average().toFloat()
            } else {
                0f
            }

            val completedJobsSnapshot = bookingsCollection
                .whereEqualTo("driverId", driverId)
                .whereEqualTo("status", "COMPLETED")
                .get()
                .await()
            
            val completedJobsCount = completedJobsSnapshot.size()

            val userProfile = com.example.goukm.ui.userprofile.UserProfileRepository.getUserProfile(driverId)
            val daysWorked = userProfile?.onlineDays?.size ?: 1 // Default to 1 to avoid division by zero

            DriverStats(
                averageRating = averageRating,
                totalReviews = ratings.size,
                totalWorkComplete = completedJobsCount,
                daysWorked = daysWorked,
                isNewDriver = ratings.isEmpty() && completedJobsCount < 5
            )
        } catch (e: Exception) {
            DriverStats()
        }
    }
}

data class DriverStats(
    val averageRating: Float = 0f,
    val totalReviews: Int = 0,
    val totalWorkComplete: Int = 0,
    val daysWorked: Int = 1,
    val isNewDriver: Boolean = true
)
