package com.example.goukm.ui.booking

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    private val journeysCollection = db.collection("journeys")

    suspend fun submitRating(rating: Rating): Result<Unit> {
        return try {
            // Check if rating for this booking already exists
            val existing = ratingsCollection.document(rating.bookingId).get().await()
            if (existing.exists()) {
                return Result.failure(Exception("Rating for this ride already exists"))
            }

            val docRef = ratingsCollection.document(rating.bookingId)
            val finalRating = rating.copy(id = docRef.id, timestamp = System.currentTimeMillis())
            docRef.set(finalRating).await()
            
            // Also update the Journey record if it exists
            JourneyRepository.updateJourneyRating(rating.bookingId, rating.rating, rating.comment)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasUserRated(bookingId: String): Boolean {
        return try {
            val doc = ratingsCollection.document(bookingId).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    private val reviewsCache = java.util.concurrent.ConcurrentHashMap<String, Pair<List<Rating>, Long>>()
    private val statsCache = java.util.concurrent.ConcurrentHashMap<String, Pair<DriverStats, Long>>()
    private val CACHE_DURATION_MS = 60_000 // 60 seconds

    suspend fun getRatingsForDriver(driverId: String, limit: Int = 20): Result<List<Rating>> = withContext(Dispatchers.Default) {
        // Return from cache if fresh
        val cached = reviewsCache[driverId]
        if (cached != null && (System.currentTimeMillis() - cached.second) < CACHE_DURATION_MS) {
            return@withContext Result.success(cached.first.take(limit))
        }

        try {
            val snapshot = ratingsCollection
                .whereEqualTo("driverId", driverId)
                .limit(500) // Safety limit
                .get()
                .await()
            
            // Sort in memory to avoid needing composite index (driverId, timestamp)
            val allRatings = snapshot.toObjects(Rating::class.java)
                .sortedByDescending { it.timestamp }
                
            reviewsCache[driverId] = allRatings to System.currentTimeMillis()
            Result.success(allRatings.take(limit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDriverStats(driverId: String): DriverStats = supervisorScope {
        // Return from cache if fresh
        val cached = statsCache[driverId]
        if (cached != null && (System.currentTimeMillis() - cached.second) < CACHE_DURATION_MS) {
            return@supervisorScope cached.first
        }

        try {
            // Use server-side simple count aggregation - no composite index usually required
            val reviewsCountDeferred = async {
                ratingsCollection.whereEqualTo("driverId", driverId).count().get(AggregateSource.SERVER).await()
            }
            
            val completedJobsCountDeferred = async {
                journeysCollection.whereEqualTo("driverId", driverId).count().get(AggregateSource.SERVER).await()
            }
            
            // Fetch ratings for average calculation - limited to 500 for memory safety
            val ratingsSnapshotDeferred = async {
                ratingsCollection.whereEqualTo("driverId", driverId)
                    .limit(500)
                    .get()
                    .await()
            }
            
            val profileDeferred = async {
                com.example.goukm.ui.userprofile.UserProfileRepository.getUserProfile(driverId)
            }

            val reviewsCountResult = reviewsCountDeferred.await()
            val completedJobsCountResult = completedJobsCountDeferred.await()
            val ratingsSnapshot = ratingsSnapshotDeferred.await()
            val userProfile = profileDeferred.await()
            
            val averageRating = withContext(Dispatchers.Default) {
                // Optimize: map rating field manually to avoid creating full objects
                val ratingValues = ratingsSnapshot.documents.mapNotNull { it.getDouble("rating") }
                if (ratingValues.isNotEmpty()) {
                    ratingValues.average().toFloat()
                } else {
                    0f
                }
            }

            val reviewsCount = reviewsCountResult.count.toInt()
            val completedJobsCount = completedJobsCountResult.count.toInt()
            val daysWorked = userProfile?.onlineDays?.size ?: 1

            val stats = DriverStats(
                averageRating = averageRating,
                totalReviews = reviewsCount,
                totalWorkComplete = completedJobsCount,
                daysWorked = daysWorked,
                isNewDriver = reviewsCount == 0 && completedJobsCount < 5
            )
            
            statsCache[driverId] = stats to System.currentTimeMillis()
            stats
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to manual count if server-side aggregation fails (rare)
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
