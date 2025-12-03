package com.example.goukm.ui.userprofile

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object UserProfileRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

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
            role_driver = doc.getBoolean("role_driver") ?: false
        )
    }

    // UPDATE user profile
    suspend fun updateUserProfile(user: UserProfile): Boolean {
        val uid = auth.currentUser?.uid ?: return false

        val updates = mapOf(
            "name" to user.name,
            "matricNumber" to user.matricNumber,
            "profilePictureUrl" to user.profilePictureUrl,
            "email" to user.email,
            "phoneNumber" to user.phoneNumber,
            "role_customer" to user.role_customer,
            "role_driver" to user.role_driver
        )

        return try {
            db.collection("users").document(uid).update(updates).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateDriverRoleTrue(): Boolean {
        return try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return false

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update("role_driver", true) // ✅ ONLY UPDATE DRIVER ROLE
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
                .update("role_customer", true)
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}
