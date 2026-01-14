package com.example.goukm.util

import com.example.goukm.ui.booking.Journey
import com.example.goukm.ui.booking.JourneyRepository
import com.example.goukm.ui.userprofile.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.random.Random

object MockDataSeeder {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val locations = listOf(
        "FST (Fakulti Sains & Teknologi)",
        "FEP (Fakulti Ekonomi & Pengurusan)",
        "FKAB (Fakulti Kejuruteraan & Alam Bina)",
        "FSSK (Fakulti Sains Sosial & Kemanusiaan)",
        "Kolej Ibrahim Yaakub",
        "Kolej Keris Mas",
        "Kolej Ungku Omar",
        "Perpustakaan Tun Seri Lanang (PTSL)",
        "Pusanika",
        "Dewan Kanselor Tun Abdul Razak (DECTAR)",
        "Masjid UKM",
        "Hospital Canselor Tuanku Muhriz (HCTM)"
    )

    private val seatTypes = listOf("Economy (4 Seats)", "Comfort (4 Seats)", "XL (6 Seats)")

    suspend fun seedMockData() {
        val currentUser = auth.currentUser ?: return
        val driverId = currentUser.uid

        // Guard: only seed if no journeys exist for this driver
        val existingJourneys = db.collection("journeys")
            .whereEqualTo("driverId", driverId)
            .limit(1)
            .get()
            .await()
        
        if (!existingJourneys.isEmpty) return

        val totalJourneys = mutableListOf<Journey>()
        val onlineWorkDurations = mutableMapOf<String, Long>()

        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.now()

        var current = startDate
        while (!current.isAfter(endDate)) {
            // Seed 1-5 rides for 60% of days
            if (Random.nextFloat() < 0.6f) {
                val numRides = Random.nextInt(1, 6)
                var dailyEarnings = 0.0
                val dateStr = current.toString() // yyyy-MM-dd

                repeat(numRides) {
                    val pickup = locations.random()
                    var dropOff = locations.random()
                    while (dropOff == pickup) dropOff = locations.random()
                    
                    val fare = Random.nextInt(5, 21).toDouble()
                    dailyEarnings += fare
                    
                    // Random hour between 8 AM and 10 PM
                    val hour = Random.nextInt(8, 22)
                    val minute = Random.nextInt(0, 60)
                    val rideTime = current.atTime(hour, minute)
                    val date = Date.from(rideTime.atZone(ZoneId.systemDefault()).toInstant())

                    val journeyId = UUID.randomUUID().toString()
                    val journey = Journey(
                        id = journeyId,
                        bookingId = "MOCK_${UUID.randomUUID().toString().take(8)}",
                        userId = "MOCK_USER_${Random.nextInt(100, 999)}",
                        driverId = driverId,
                        pickup = pickup,
                        dropOff = dropOff,
                        offeredFare = fare.toInt().toString(),
                        seatType = seatTypes.random(),
                        timestamp = date,
                        paymentMethod = if (Random.nextBoolean()) "QR_DUITNOW" else "CASH",
                        paymentStatus = "PAID",
                        rating = (30 + Random.nextInt(0, 21)).toFloat() / 10f, // 3.0 to 5.0
                        comment = if (Random.nextBoolean()) "Good ride!" else ""
                    )
                    totalJourneys.add(journey)
                }

                // Random online duration between 30 and 480 minutes
                onlineWorkDurations[dateStr] = Random.nextLong(30, 481)
            }
            current = current.plusDays(1)
        }

        // Batch write journeys (Firestore limit 500 per batch)
        val batches = totalJourneys.chunked(500)
        for (batch in batches) {
            val firestoreBatch = db.batch()
            for (journey in batch) {
                val docRef = db.collection("journeys").document(journey.id)
                firestoreBatch.set(docRef, journey)
            }
            firestoreBatch.commit().await()
        }

        // Update user profile with online work durations
        val userRef = db.collection("users").document(driverId)
        val profileDoc = userRef.get().await()
        if (profileDoc.exists()) {
            val existingDurations = (profileDoc.get("onlineWorkDurations") as? Map<String, Long>) ?: emptyMap()
            val mergedDurations = existingDurations + onlineWorkDurations
            userRef.update("onlineWorkDurations", mergedDurations).await()
        }
    }
}
