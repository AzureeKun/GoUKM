package com.example.goukm.ui.form

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class DriverApplicationViewModel : ViewModel() {
    // Vehicle info
    var licenseNumber by mutableStateOf("")
    var vehiclePlateNumber by mutableStateOf("")
    var vehicleType by mutableStateOf("Motorcycle")

    // Image selections
    var icFrontUri by mutableStateOf<Uri?>(null)
    var icBackUri by mutableStateOf<Uri?>(null)
    var drivingLicenseUri by mutableStateOf<Uri?>(null)
    var vehicleInsuranceUri by mutableStateOf<Uri?>(null)
    var bankQrUri by mutableStateOf<Uri?>(null)

    var isSubmitting by mutableStateOf(false)
    var lastError by mutableStateOf<String?>(null)

    fun setVehicleInfo(license: String, plate: String, type: String) {
        licenseNumber = license
        vehiclePlateNumber = plate
        vehicleType = type
    }

    fun setIc(front: Uri?, back: Uri?) {
        icFrontUri = front
        icBackUri = back
    }

    fun setDocuments(driving: Uri?, insurance: Uri?, bank: Uri?) {
        drivingLicenseUri = driving
        vehicleInsuranceUri = insurance
        bankQrUri = bank
    }

    suspend fun submitApplication(context: Context): Boolean {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        val firestore = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance().reference

        if (licenseNumber.isBlank() || vehiclePlateNumber.isBlank()) {
            lastError = "Please complete vehicle and license details."
            return false
        }

        val requiredImages = listOf(
            icFrontUri,
            icBackUri,
            drivingLicenseUri,
            vehicleInsuranceUri,
            bankQrUri
        )

        if (requiredImages.any { it == null }) {
            lastError = "Please attach all required documents."
            return false
        }

        isSubmitting = true
        lastError = null

        return try {
            val uploads = withContext(Dispatchers.IO) {
                mapOf(
                    "ic_front" to icFrontUri,
                    "ic_back" to icBackUri,
                    "driving_license" to drivingLicenseUri,
                    "vehicle_insurance" to vehicleInsuranceUri,
                    "bank_qr" to bankQrUri
                ).mapValues { (name, uri) ->
                    val path = "driverApplications/$uid/$name.jpg"
                    val bytes = compressImage(context, uri!!)
                    storage.child(path).putBytes(bytes).await()
                    path
                }
            }

            val data = hashMapOf(
                "uid" to uid,
                "licenseNumber" to licenseNumber,
                "vehiclePlateNumber" to vehiclePlateNumber,
                "vehicleType" to vehicleType,
                "status" to "under_review",
                "submittedAt" to FieldValue.serverTimestamp(),
                "documents" to uploads
            )

            firestore.collection("driverApplications").document(uid).set(data).await()
            true
        } catch (e: Exception) {
            lastError = e.message ?: "Upload failed"
            false
        } finally {
            isSubmitting = false
        }
    }

    private fun compressImage(context: Context, uri: Uri): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open image")
        inputStream.use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
                ?: throw IllegalArgumentException("Unable to decode image")
            val output = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
            return output.toByteArray()
        }
    }
}
