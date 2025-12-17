package com.example.goukm.ui.register

import com.example.goukm.api.MockSMPWebAPI
import com.example.goukm.api.UKMStudent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object RegistrationRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // STEP 1: CHECK IF MATRIC NUMBER EXISTS IN GOUKM DATABASE
    suspend fun checkMatricExists(matricNumber: String): Boolean {
        val normalizedMatric = matricNumber.uppercase().trim()
        val q = db.collection("users")
            .whereEqualTo("matricNumber", normalizedMatric)
            .get()
            .await()

        return !q.isEmpty
    }
    
    // Legacy method for backward compatibility
    suspend fun checkEmailExists(email: String): Boolean {
        val q = db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .await()

        return !q.isEmpty
    }

    // STEP 2: LOGIN EXISTING USER
    // First checks GoUKM database, then falls back to Mock SMPWeb if not found
    suspend fun loginUser(matricNumber: String, password: String): Result<String> {
        return try {
            // Normalize matric number
            val normalizedMatric = matricNumber.uppercase().trim()
            val email = "${normalizedMatric.lowercase()}@siswa.ukm.edu.my"
            
            // Check if user exists in GoUKM database
            val userQuery = db.collection("users")
                .whereEqualTo("matricNumber", normalizedMatric)
                .get()
                .await()
            
            if (!userQuery.isEmpty) {
                // User exists in GoUKM - verify password with Firebase
                val doc = userQuery.documents.first()
                val storedEmail = doc.getString("email") ?: email
                
                val res = auth.signInWithEmailAndPassword(storedEmail, password).await()
                val uid = res.user!!.uid
                Result.success(uid)
            } else {
                // User doesn't exist in GoUKM - check Mock SMPWeb
                val smpWebResponse = MockSMPWebAPI.authenticate(normalizedMatric, password)
                
                if (smpWebResponse.success && smpWebResponse.student != null) {
                    // Store student data temporarily for registration flow
                    // Return special result to indicate new user needs registration
                    Result.failure(Exception("NEW_USER_FROM_SMPWEB:${smpWebResponse.student.matricNumber}"))
                } else {
                    Result.failure(Exception(smpWebResponse.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if matric exists in Mock SMPWeb (without password verification)
     * Used to determine if user should go through registration flow
     */
    suspend fun checkSMPWebExists(matricNumber: String): UKMStudent? {
        val normalizedMatric = matricNumber.uppercase().trim()
        return MockSMPWebAPI.getStudentProfile(normalizedMatric)
    }
    
    /**
     * Register a new user in GoUKM database using data from Mock SMPWeb
     */
    private suspend fun registerUserFromSMPWeb(student: UKMStudent, password: String): String {
        // Create Firebase Auth user
        val user = auth.createUserWithEmailAndPassword(student.email, password).await()
        val uid = user.user!!.uid
        
        // Store user data in Firestore
        val userData = hashMapOf(
            "uid" to uid,
            "email" to student.email,
            "name" to student.fullName,
            "phoneNumber" to student.phoneNumber,
            "matricNumber" to student.matricNumber,
            "faculty" to student.faculty,
            "academicProgram" to student.academicProgram,
            "yearOfStudy" to student.yearOfStudy,
            "enrolmentLevel" to student.enrolmentLevel,
            "academicStatus" to student.academicStatus,
            "batch" to student.batch,
            "role_customer" to true,  // Default role
            "role_driver" to false,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        
        db.collection("users").document(uid).set(userData).await()
        return uid
    }

    suspend fun loginUserWithoutBroker(matricNumber: String, password: String): Result<String> {
        // Same as loginUser but without broker (for fallback)
        return loginUser(matricNumber, password)
    }

    // STEP 3: REGISTER NEW USER AFTER ROLE SELECTED
    suspend fun createUserWithRole(
        email: String,
        password: String,
        phone: String,
        name: String,
        role: String,
        smpWebStudent: UKMStudent? = null
    ): Result<String> {
        return try {
            val user = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = user.user!!.uid

            val matricNumber = email.substringBefore("@siswa.ukm.edu.my").uppercase()
            val isDriver = role == "driver"

            // Use SMPWeb student data if available, otherwise use provided data
            val userData = if (smpWebStudent != null) {
                hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "name" to name,
                    "phoneNumber" to phone,
                    "matricNumber" to matricNumber,
                    "faculty" to smpWebStudent.faculty,
                    "academicProgram" to smpWebStudent.academicProgram,
                    "yearOfStudy" to smpWebStudent.yearOfStudy,
                    "enrolmentLevel" to smpWebStudent.enrolmentLevel,
                    "academicStatus" to smpWebStudent.academicStatus,
                    "batch" to smpWebStudent.batch,
                    "role_customer" to true,
                    "role_driver" to isDriver,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
            } else {
                hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "name" to name,
                    "phoneNumber" to phone,
                    "matricNumber" to matricNumber,
                    "role_customer" to true,
                    "role_driver" to isDriver,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
            }

            db.collection("users").document(uid).set(userData).await()

            Result.success(uid)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}
