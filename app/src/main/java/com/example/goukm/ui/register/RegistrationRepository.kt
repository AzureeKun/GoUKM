package com.example.goukm.ui.register

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun registerUserAndSave(
    email: String,
    password: String,
    phoneNumber: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                val user = authTask.result?.user
                val uid = user?.uid

                if (uid != null) {
                    val matricNumber = email.substringBefore("@siswa.ukm.edu.my")

                    val userData = hashMapOf(
                        "uid" to uid,
                        "email" to email,
                        "matricNumber" to matricNumber,
                        "phoneNumber" to phoneNumber,
                        "role" to "student",
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )

                    db.collection("users").document(uid).set(userData)
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            onFailure("Failed to save user data: ${e.message}")
                        }
                } else {
                    onFailure("Authentication succeeded but UID is null")
                }
            } else {
                onFailure("Registeration Failed: ${authTask.exception?.message}")
            }
        }
}