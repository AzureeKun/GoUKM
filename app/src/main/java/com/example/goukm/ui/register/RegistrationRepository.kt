package com.example.goukm.ui.register

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object RegistrationRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // STEP 1: CHECK IF EMAIL EXISTS
    suspend fun checkEmailExists(email: String): Boolean {
        val q = db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .await()

        return !q.isEmpty
    }

    // STEP 2: LOGIN EXISTING USER
    suspend fun loginUser(email: String, password: String): Result<String> {
        return try {
            val res = auth.signInWithEmailAndPassword(email, password).await()
            val uid = res.user!!.uid
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // STEP 3: REGISTER NEW USER AFTER ROLE SELECTED
    suspend fun createUserWithRole(
        email: String,
        password: String,
        phone: String,
        name: String,
        role: String
    ): Result<String> {
        return try {
            val user = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = user.user!!.uid

            val matricNumber = email.substringBefore("@siswa.ukm.edu.my")
            val isDriver = role == "driver"

            val userData = hashMapOf(
                "uid" to uid,
                "email" to email,
                "name" to name,
                "phoneNumber" to phone,
                "matricNumber" to matricNumber,
                "role_customer" to true,      // semua user adalah customer
                "role_driver" to isDriver,    // hanya driver dapat TRUE
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            db.collection("users").document(uid).set(userData).await()

            Result.success(uid)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
