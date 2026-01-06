package com.example.goukm.util

import com.example.goukm.ui.userprofile.UserProfile

/**
 * Utility class to check driver application eligibility based on UKM regulations
 */
object DriverEligibilityChecker {
    
    /**
     * Check if a user is eligible to apply as a driver
     * 
     * UKM Regulations:
     * - Only second-year students and above (batch A21 or earlier) are eligible
     * - First-year students (A22 and above) are NOT eligible
     * - Alumni (A18 and earlier with status "Tamat Pengajian") are NOT eligible
     * - User must have active enrolment status ("Mendaftar")
     * - User must be a current undergraduate student
     * 
     * @param user The user profile to check
     * @return EligibilityResult with eligibility status and reason
     */
    fun checkEligibility(user: UserProfile): EligibilityResult {
        // Check if batch is valid
        if (user.batch.isBlank()) {
            return EligibilityResult(
                isEligible = false,
                reason = "Unable to determine academic batch. Please contact support."
            )
        }
        
        // Extract batch number (e.g., "A20" -> 20, "A21" -> 21, "A22" -> 22)
        val batchNum = user.batch.substring(1).toIntOrNull()
        
        if (batchNum == null) {
            return EligibilityResult(
                isEligible = false,
                reason = "Invalid batch format. Please contact support."
            )
        }
        
        // Check if user is first-year (A22 and above)
        if (batchNum >= 22) {
            return EligibilityResult(
                isEligible = false,
                reason = "First-year students are not eligible to apply as drivers. Only second-year students and above can apply."
            )
        }
        
        // Check if user is alumni (A18 and earlier)
        if (batchNum <= 18) {
            // Check academic status for alumni
            if (user.academicStatus == "Tamat Pengajian") {
                return EligibilityResult(
                    isEligible = false,
                    reason = "Alumni (graduated students) are not eligible to apply as drivers."
                )
            }
        }
        
        // Check enrolment status
        if (user.academicStatus != "Mendaftar") {
            return EligibilityResult(
                isEligible = false,
                reason = "Only students with active enrolment status (Mendaftar) are eligible to apply as drivers."
            )
        }
        
        // Check if user is at least second-year (A21 or earlier, but not A22+)
        // A20 = 3rd year, A21 = 2nd year - both eligible
        if (batchNum >= 19 && batchNum <= 21) {
            return EligibilityResult(
                isEligible = true,
                reason = "You are eligible to apply as a driver."
            )
        }
        
        // For older batches (A18 and earlier), allow if "Mendaftar" (e.g., extending studies)
        // Disallow if "Tamat Pengajian" (handled above)
        if (batchNum <= 18 && user.academicStatus == "Mendaftar") {
            return EligibilityResult(
                isEligible = true,
                reason = "You are eligible to apply as a driver."
            )
        }
        
        return EligibilityResult(
            isEligible = false,
            reason = "You are not eligible to apply as a driver based on your academic status."
        )
    }
    
    /**
     * Get a user-friendly message explaining eligibility requirements
     */
    fun getEligibilityRequirementsMessage(): String {
        return """
            Driver Application Eligibility Requirements:
            
            • Must be a second-year student or above (Batch A21 or earlier)
            • Must have active enrolment status (Mendaftar)
            • First-year students (Batch A22 and above) are not eligible
            • Alumni (Batch A18 and earlier with status "Tamat Pengajian") are not eligible
        """.trimIndent()
    }
}

/**
 * Result of eligibility check
 */
data class EligibilityResult(
    val isEligible: Boolean,
    val reason: String
)

