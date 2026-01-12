package com.example.goukm.ui.userprofile

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

object UserProfileRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    // get user profile
    suspend fun getUserProfile(uid: String? = null): UserProfile? {
        val targetUid = uid ?: auth.currentUser?.uid ?: return null

        val doc = db.collection("users").document(targetUid).get().await()
        if (!doc.exists()) return null

        val vehiclesList = (doc.get("vehicles") as? List<Map<String, Any>>)?.map {
            Vehicle(
                id = (it["id"] as? String) ?: "",
                brand = (it["brand"] as? String) ?: "",
                color = (it["color"] as? String) ?: "",
                plateNumber = (it["plateNumber"] as? String) ?: "",
                licenseNumber = (it["licenseNumber"] as? String) ?: "",
                grantUrl = (it["grantUrl"] as? String) ?: "",
                status = (it["status"] as? String) ?: "Approved",
                lastEditedAt = (it["lastEditedAt"] as? Long) ?: 0L
            )
        } ?: emptyList()

        // Deduplicate vehicles by plate number immediately to clean up existing data
        val uniqueVehicles = vehiclesList.distinctBy { it.plateNumber.uppercase().trim() }

        return UserProfile(
            uid = targetUid,
            name = doc.getString("name") ?: "",
            matricNumber = doc.getString("matricNumber") ?: "",
            profilePictureUrl = doc.getString("profilePictureUrl"),
            email = doc.getString("email") ?: "",
            phoneNumber = doc.getString("phoneNumber") ?: "",
            role_customer = doc.getBoolean("role_customer") ?: true,
            role_driver = doc.getBoolean("role_driver") ?: false,
            licenseNumber = doc.getString("licenseNumber") ?: "",
            vehiclePlateNumber = doc.getString("vehiclePlateNumber") ?: "",
            vehicleType = doc.getString("vehicleType") ?: "",
            carBrand = doc.getString("carBrand") ?: "",
            carColor = doc.getString("carColor") ?: "",
            faculty = doc.getString("faculty") ?: "",
            academicProgram = doc.getString("academicProgram") ?: "",
            yearOfStudy = (doc.getLong("yearOfStudy") ?: 0).toInt(),
            enrolmentLevel = doc.getString("enrolmentLevel") ?: "",
            academicStatus = doc.getString("academicStatus") ?: "",
            batch = doc.getString("batch") ?: "",
            isAvailable = doc.getBoolean("isAvailable") ?: false,
            onlineDays = doc.get("onlineDays") as? List<String> ?: emptyList(),
            onlineWorkDurations = (doc.get("onlineWorkDurations") as? Map<String, Long>) ?: emptyMap(),
            vehicles = uniqueVehicles,
            preferredPaymentMethod = doc.getString("preferredPaymentMethod") ?: "CASH"
        ).let { initialUser ->
            var user = initialUser

            // --- 1. FIX/REPAIR LOGIC (Existing) ---
            // If user is a driver but essential details are missing (common for old accounts),
            // proactively fetch from their application and sync it to the user profile.
            val missingInfo = user.role_driver && (
                user.carBrand.isEmpty() || 
                user.carColor.isEmpty() || 
                user.licenseNumber.isEmpty() || 
                user.vehiclePlateNumber.isEmpty()
            )

            if (missingInfo) {
                try {
                    val appDoc = db.collection("driverApplications").document(targetUid).get().await()
                    if (appDoc.exists()) {
                        val brand = appDoc.getString("carBrand") ?: ""
                        val color = appDoc.getString("carColor") ?: ""
                        val license = appDoc.getString("licenseNumber") ?: ""
                        val plate = appDoc.getString("vehiclePlateNumber") ?: ""

                        val updates = mutableMapOf<String, Any>()
                        if (user.carBrand.isEmpty() && brand.isNotEmpty()) updates["carBrand"] = brand
                        if (user.carColor.isEmpty() && color.isNotEmpty()) updates["carColor"] = color
                        if (user.licenseNumber.isEmpty() && license.isNotEmpty()) updates["licenseNumber"] = license
                        if (user.vehiclePlateNumber.isEmpty() && plate.isNotEmpty()) updates["vehiclePlateNumber"] = plate

                        if (updates.isNotEmpty()) {
                            // Update Firestore proactively for next time
                            db.collection("users").document(targetUid).update(updates)
                            
                            // Return the "repaired" user object for immediate UI update
                            user = user.copy(
                                carBrand = if (user.carBrand.isEmpty()) brand else user.carBrand,
                                carColor = if (user.carColor.isEmpty()) color else user.carColor,
                                licenseNumber = if (user.licenseNumber.isEmpty()) license else user.licenseNumber,
                                vehiclePlateNumber = if (user.vehiclePlateNumber.isEmpty()) plate else user.vehiclePlateNumber
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // --- 2. MIGRATE LEGACY VEHICLE TO LIST ---
            // If vehicles list is empty but single-field data exists, migrate it to list.
            if (user.role_driver && user.vehicles.isEmpty() && user.vehiclePlateNumber.isNotEmpty()) {
                 val legacyVehicle = Vehicle(
                    id = java.util.UUID.randomUUID().toString(),
                    brand = user.carBrand.ifEmpty { "Unknown Brand" },
                    color = user.carColor.ifEmpty { "Unknown Color" },
                    plateNumber = user.vehiclePlateNumber,
                    licenseNumber = user.licenseNumber
                 )
                 
                 // Update local user object
                 user = user.copy(vehicles = listOf(legacyVehicle))
                 
                 // Persist to Firestore
                 val vehicleMap = mapOf(
                    "id" to legacyVehicle.id,
                    "brand" to legacyVehicle.brand,
                    "color" to legacyVehicle.color,
                    "plateNumber" to legacyVehicle.plateNumber,
                    "licenseNumber" to legacyVehicle.licenseNumber,
                    "lastEditedAt" to legacyVehicle.lastEditedAt
                )
                 try {
                     db.collection("users").document(targetUid).update(
                        "vehicles", com.google.firebase.firestore.FieldValue.arrayUnion(vehicleMap)
                     )
                 } catch (e: Exception) {
                     e.printStackTrace()
                 }
            }

            user
        }
    }

    // Upload gambar ke Storage dan return download URL
    suspend fun uploadProfilePicture(uid: String, uri: Uri): String {
        val fileRef = storage.child("profile_pictures/$uid/profile.jpg")
        fileRef.putFile(uri).await()
        return fileRef.downloadUrl.await().toString()
    }

    suspend fun uploadVehicleGrant(uid: String, uri: Uri): String {
        val fileRef = storage.child("vehicle_grant/$uid/${System.currentTimeMillis()}.jpg")
        fileRef.putFile(uri).await()
        return fileRef.downloadUrl.await().toString()
    }

    // UPDATE user profile di Firestore
    suspend fun updateUserProfile(user: UserProfile): Boolean {
        val uid = auth.currentUser?.uid ?: return false

        val updates = mapOf(
            "name" to user.name,
            "matricNumber" to user.matricNumber,
            "profilePictureUrl" to user.profilePictureUrl,
            "email" to user.email,
            "phoneNumber" to user.phoneNumber,
            "role_customer" to user.role_customer,
            "role_driver" to user.role_driver,
            "licenseNumber" to user.licenseNumber,
            "vehiclePlateNumber" to user.vehiclePlateNumber,
            "vehicleType" to user.vehicleType,
            "carBrand" to user.carBrand,
            "carColor" to user.carColor,
            "faculty" to user.faculty,
            "academicProgram" to user.academicProgram,
            "yearOfStudy" to user.yearOfStudy,
            "enrolmentLevel" to user.enrolmentLevel,
            "academicStatus" to user.academicStatus,
            "batch" to user.batch,
            "onlineWorkDurations" to user.onlineWorkDurations,
            "preferredPaymentMethod" to user.preferredPaymentMethod,
            "vehicles" to user.vehicles.map {
                mapOf(
                    "id" to it.id,
                    "brand" to it.brand,
                    "color" to it.color,
                    "plateNumber" to it.plateNumber,
                    "licenseNumber" to it.licenseNumber,
                    "lastEditedAt" to it.lastEditedAt
                )
            }
        )

        return try {
            db.collection("users").document(uid).update(updates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateDriverRoleTrue(): Boolean {
        return try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return false

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update(
                    mapOf(
                        "role_driver" to true,
                        "role_customer" to true // keep both so user can switch modes
                    )
                ) // ✅ Enable driver while preserving customer access
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateCustomerRoleTrue(): Boolean {
        return try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return false

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update(
                    mapOf(
                        "role_customer" to true,
                        "role_driver" to true // do not remove driver access when switching view
                    )
                )
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateDriverAvailability(isAvailable: Boolean): Boolean {
        return try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return false

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update("isAvailable", isAvailable)
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun saveFCMToken(token: String) {
        val uid = auth.currentUser?.uid ?: return
        try {
            val data = mapOf("fcmToken" to token)
            db.collection("users").document(uid)
                .set(data, com.google.firebase.firestore.SetOptions.merge())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun recordOnlineDay(uid: String) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        try {
            db.collection("users").document(uid)
                .update("onlineDays", com.google.firebase.firestore.FieldValue.arrayUnion(today))
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateOnlineDuration(uid: String, durationMinutes: Long) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        try {
            val userRef = db.collection("users").document(uid)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentDurations = (snapshot.get("onlineWorkDurations") as? Map<String, Long>)?.toMutableMap() ?: mutableMapOf()
                val currentDayDuration = currentDurations[today] ?: 0L
                currentDurations[today] = currentDayDuration + durationMinutes
                transaction.update(userRef, "onlineWorkDurations", currentDurations)
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun switchVehicle(plateNumber: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val user = getUserProfile(uid) ?: return false
            val vehicle = user.vehicles.find { it.plateNumber == plateNumber } ?: return false
            
            db.collection("users").document(uid).update(
                mapOf(
                    "carBrand" to vehicle.brand,
                    "carColor" to vehicle.color,
                    "vehiclePlateNumber" to vehicle.plateNumber,
                    "licenseNumber" to vehicle.licenseNumber
                )
            ).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun addNewVehicle(vehicle: Vehicle): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val user = getUserProfile(uid)
            if (user != null && user.vehicles.any { it.plateNumber.uppercase().trim() == vehicle.plateNumber.uppercase().trim() }) {
                return true // Already exists, consider it a success
            }

            val vehicleMap = mapOf(
                "id" to vehicle.id,
                "brand" to vehicle.brand,
                "color" to vehicle.color,
                "plateNumber" to vehicle.plateNumber.uppercase().trim(),
                "licenseNumber" to vehicle.licenseNumber,
                "grantUrl" to vehicle.grantUrl,
                "status" to vehicle.status,
                "lastEditedAt" to vehicle.lastEditedAt
            )
            db.collection("users").document(uid).update(
                "vehicles", com.google.firebase.firestore.FieldValue.arrayUnion(vehicleMap)
            ).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }



    suspend fun updateVehicle(updatedVehicle: Vehicle): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val user = getUserProfile(uid) ?: return false
            
            // Re-construct the entire list with the updated vehicle
            val updatedVehiclesList = user.vehicles.map {
                if (it.id == updatedVehicle.id) updatedVehicle else it
            }.map { 
                mapOf(
                    "id" to it.id,
                    "brand" to it.brand,
                    "color" to it.color,
                    "plateNumber" to it.plateNumber,
                    "licenseNumber" to it.licenseNumber,
                    "grantUrl" to it.grantUrl,
                    "status" to it.status,
                    "lastEditedAt" to it.lastEditedAt
                )
            }

            db.collection("users").document(uid).update("vehicles", updatedVehiclesList).await()

            // If updating currently active vehicle, update the profile top-level fields too
            // NOTE: Do NOT update licenseNumber here. The driver's license ID is personal and should not change
            // just because a vehicle is edited. It remains the one registered at onboarding.
            if (user.vehiclePlateNumber == updatedVehicle.plateNumber || user.vehicles.find { it.id == updatedVehicle.id }?.plateNumber == user.vehiclePlateNumber) {
                 db.collection("users").document(uid).update(
                    mapOf(
                        "carBrand" to updatedVehicle.brand,
                        "carColor" to updatedVehicle.color,
                        "vehiclePlateNumber" to updatedVehicle.plateNumber
                    )
                )
                if (updatedVehicle.status == "Approved") {
                    db.collection("users").document(uid).update(
                        mapOf(
                            "carBrand" to updatedVehicle.brand,
                            "carColor" to updatedVehicle.color,
                            "vehiclePlateNumber" to updatedVehicle.plateNumber
                        )
                    )
                } else if (user.vehiclePlateNumber == updatedVehicle.plateNumber) {
                     // If the active vehicle became pending, clear the active status
                     db.collection("users").document(uid).update(
                        mapOf(
                            "carBrand" to "",
                            "carColor" to "",
                            "vehiclePlateNumber" to ""
                        )
                    )
                }
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteVehicle(vehicleId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val user = getUserProfile(uid) ?: return false
            val vehicleToDelete = user.vehicles.find { it.id == vehicleId } ?: return false

            // REMOVE from vehicles list by ID
            val updatedVehicles = user.vehicles.filter { it.id != vehicleId }.map { 
                mapOf(
                    "id" to it.id,
                    "brand" to it.brand,
                    "color" to it.color,
                    "plateNumber" to it.plateNumber,
                    "licenseNumber" to it.licenseNumber,
                    "grantUrl" to it.grantUrl,
                    "status" to it.status,
                    "lastEditedAt" to it.lastEditedAt
                )
            }

            val batch = db.batch()
            val userRef = db.collection("users").document(uid)
            batch.update(userRef, "vehicles", updatedVehicles)

            // If this was the active vehicle, clear the active vehicle fields
            if (user.vehiclePlateNumber == vehicleToDelete.plateNumber) {
                batch.update(userRef, mapOf(
                    "vehiclePlateNumber" to "",
                    "carBrand" to "",
                    "carColor" to ""
                ))
            }
            
            batch.commit().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun submitVehicleApplication(matricNumber: String, vehicle: Vehicle): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val applicationMap = mapOf(
                "id" to vehicle.id,
                "userId" to uid,
                "matricNumber" to matricNumber,
                "brand" to vehicle.brand,
                "color" to vehicle.color,
                "plateNumber" to vehicle.plateNumber,
                "licenseNumber" to vehicle.licenseNumber,
                "grantUrl" to vehicle.grantUrl,
                "status" to "Pending",
                "submittedAt" to System.currentTimeMillis()
            )
            // Use plateNumber as document ID to avoid duplicate overlapping applications for same car
            db.collection("newVehicleApplications").document(vehicle.plateNumber).set(applicationMap).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updatePreferredPaymentMethod(method: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            db.collection("users").document(uid).update("preferredPaymentMethod", method).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
