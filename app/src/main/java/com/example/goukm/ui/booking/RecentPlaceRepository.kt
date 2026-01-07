package com.example.goukm.ui.booking

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

data class RecentPlace(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val timestamp: Date = Date(),
    val usageCount: Int = 1
)

object RecentPlaceRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private fun getCollection() = auth.currentUser?.let {
        firestore.collection("users").document(it.uid).collection("recent_places")
    }

    suspend fun savePlace(name: String, address: String, lat: Double, lng: Double) {
        val collection = getCollection() ?: return
        
        try {
            // Check if place already exists (by name or address)
            val existing = collection
                .whereEqualTo("address", address)
                .get()
                .await()
            
            if (!existing.isEmpty) {
                val doc = existing.documents.first()
                val count = doc.getLong("usageCount")?.toInt() ?: 1
                doc.reference.update(
                    mapOf(
                        "timestamp" to Date(),
                        "usageCount" to count + 1,
                        "name" to name // Update name in case it changed
                    )
                ).await()
            } else {
                val id = collection.document().id
                val place = RecentPlace(
                    id = id,
                    name = name,
                    address = address,
                    lat = lat,
                    lng = lng,
                    timestamp = Date(),
                    usageCount = 1
                )
                collection.document(id).set(place).await()
            }
            
            // Optional: Cleanup old places if more than 10
            cleanupOldPlaces()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun cleanupOldPlaces() {
        val collection = getCollection() ?: return
        try {
            val all = collection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            if (all.size() > 10) {
                val toDelete = all.documents.drop(10)
                firestore.runBatch { batch ->
                    toDelete.forEach { batch.delete(it.reference) }
                }.await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getRecentPlaces(): List<RecentPlace> {
        val collection = getCollection() ?: return emptyList()
        return try {
            collection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(RecentPlace::class.java) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
