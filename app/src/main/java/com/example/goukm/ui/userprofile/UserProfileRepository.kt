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

    // GET user profile
    suspend fun getUserProfile(): UserProfile? {
        val uid = auth.currentUser?.uid ?: return null

        val doc = db.collection("users").document(uid).get().await()
        if (!doc.exists()) return null

        return UserProfile(
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
            faculty = doc.getString("faculty") ?: "",
            academicProgram = doc.getString("academicProgram") ?: "",
            yearOfStudy = (doc.getLong("yearOfStudy") ?: 0).toInt(),
            enrolmentLevel = doc.getString("enrolmentLevel") ?: "",
            academicStatus = doc.getString("academicStatus") ?: "",
            batch = doc.getString("batch") ?: "",
            isAvailable = doc.getBoolean("isAvailable") ?: false
        )
    }

    // Upload gambar ke Storage dan return download URL
    suspend fun uploadProfilePicture(uid: String, uri: Uri): String {
        val fileRef = storage.child("profile_pictures/$uid/profile.jpg")
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
            "faculty" to user.faculty,
            "academicProgram" to user.academicProgram,
            "yearOfStudy" to user.yearOfStudy,
            "enrolmentLevel" to user.enrolmentLevel,
            "academicStatus" to user.academicStatus,
            "batch" to user.batch
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

}
