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
            phoneNumber = doc.getString("phoneNumber") ?: ""
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
            "phoneNumber" to user.phoneNumber
        )

        return try {
            db.collection("users").document(uid).update(updates).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
