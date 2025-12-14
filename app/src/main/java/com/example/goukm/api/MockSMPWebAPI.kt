package com.example.goukm.api

import java.security.MessageDigest

/**
 * Data class representing a UKM student record from SMPWeb
 */
data class UKMStudent(
    val matricNumber: String,
    val passwordHash: String, // Hashed password
    val fullName: String,
    val email: String, // @siswa.ukm.edu.my
    val phoneNumber: String,
    val faculty: String,
    val academicProgram: String,
    val yearOfStudy: Int,
    val enrolmentLevel: String, // e.g., "Sarjana Muda", "Sarjana"
    val academicStatus: String, // "Mendaftar" (enrolled) or "Tamat Pengajian" (graduated)
    val batch: String // Derived from matric number (e.g., "A20", "A21", "A22")
)

/**
 * Response from Mock SMPWeb API authentication
 */
data class SMPWebAuthResponse(
    val success: Boolean,
    val student: UKMStudent? = null,
    val message: String = ""
)

/**
 * Mock SMPWeb API Service
 * Simulates the actual SMPWeb authentication and data retrieval
 */
object MockSMPWebAPI {
    
    // Dummy dataset of UKM students
    private val studentDatabase = generateMockStudents()
    
    /**
     * Authenticate a student using matric number and password
     */
    fun authenticate(matricNumber: String, password: String): SMPWebAuthResponse {
        val normalizedMatric = matricNumber.uppercase().trim()
        
        // Find student by matric number
        val student = studentDatabase.find { it.matricNumber == normalizedMatric }
        
        if (student == null) {
            return SMPWebAuthResponse(
                success = false,
                message = "Invalid matriculation number"
            )
        }
        
        // Verify password hash
        val passwordHash = hashPassword(password)
        if (student.passwordHash != passwordHash) {
            return SMPWebAuthResponse(
                success = false,
                message = "Invalid password"
            )
        }
        
        return SMPWebAuthResponse(
            success = true,
            student = student,
            message = "Authentication successful"
        )
    }
    
    /**
     * Get student profile by matric number (without password verification)
     * Used for profile retrieval after authentication
     */
    fun getStudentProfile(matricNumber: String): UKMStudent? {
        val normalizedMatric = matricNumber.uppercase().trim()
        return studentDatabase.find { it.matricNumber == normalizedMatric }
    }
    
    /**
     * Hash password using SHA-256 (simple hash for mock purposes)
     */
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Generate batch from matric number
     * Format: A + 6 digits (e.g., A203399 -> batch "A20")
     */
    private fun extractBatch(matricNumber: String): String {
        if (matricNumber.length >= 3 && matricNumber.startsWith("A", ignoreCase = true)) {
            return matricNumber.substring(0, 3).uppercase()
        }
        return "A20" // Default batch
    }
    
    /**
     * Determine academic year from batch
     * A20 = 3rd year, A21 = 2nd year, A22+ = 1st year, A18- = Alumni
     */
    private fun determineYearOfStudy(batch: String): Int {
        val batchNum = batch.substring(1).toIntOrNull() ?: 20
        return when {
            batchNum >= 22 -> 1 // First year
            batchNum == 21 -> 2 // Second year
            batchNum == 20 -> 3 // Third year
            batchNum == 19 -> 4 // Fourth year
            else -> 0 // Alumni or unknown
        }
    }
    
    /**
     * Determine academic status based on batch
     */
    private fun determineAcademicStatus(batch: String): String {
        val batchNum = batch.substring(1).toIntOrNull() ?: 20
        return if (batchNum <= 18) {
            "Tamat Pengajian" // Graduated
        } else {
            "Mendaftar" // Enrolled
        }
    }
    
    /**
     * Generate at least 100 realistic UKM student records with wider matric number range
     * Password format: UKM + matric number (e.g., UKMA198009)
     */
    private fun generateMockStudents(): List<UKMStudent> {
        val students = mutableListOf<UKMStudent>()
        
        // Common UKM faculties
        val faculties = listOf(
            "Fakulti Sains dan Teknologi",
            "Fakulti Kejuruteraan dan Alam Bina",
            "Fakulti Ekonomi dan Pengurusan",
            "Fakulti Sains Sosial dan Kemanusiaan",
            "Fakulti Perubatan",
            "Fakulti Pergigian",
            "Fakulti Farmasi",
            "Fakulti Undang-Undang",
            "Fakulti Pendidikan",
            "Fakulti Teknologi dan Sains Maklumat"
        )
        
        // Common academic programs
        val programs = listOf(
            "Sarjana Muda Sains Komputer",
            "Sarjana Muda Kejuruteraan Elektrik",
            "Sarjana Muda Ekonomi",
            "Sarjana Muda Sastera",
            "Sarjana Muda Perubatan",
            "Sarjana Muda Pergigian",
            "Sarjana Muda Farmasi",
            "Sarjana Muda Undang-Undang",
            "Sarjana Muda Pendidikan",
            "Sarjana Muda Teknologi Maklumat"
        )
        
        // Common Malay names
        val firstNames = listOf(
            "Ahmad", "Muhammad", "Nur", "Siti", "Fatimah", "Ali", "Hassan", "Aminah",
            "Zainab", "Ibrahim", "Ismail", "Yusuf", "Aisyah", "Khadijah", "Omar",
            "Hafiz", "Amir", "Sara", "Lina", "Nora", "Diana", "Rina", "Zara",
            "Faris", "Adam", "Hakim", "Razak", "Azman", "Rahman", "Zulkifli"
        )
        
        val lastNames = listOf(
            "bin Abdullah", "binti Abdullah", "bin Ahmad", "binti Ahmad",
            "bin Hassan", "binti Hassan", "bin Ismail", "binti Ismail",
            "bin Mohd", "binti Mohd", "bin Ali", "binti Ali",
            "bin Omar", "binti Omar", "bin Yusuf", "binti Yusuf"
        )
        
        // Generate students across different batches with wider range
        // Using a mix of sequential and scattered numbers for better coverage
        // Includes specific test cases: A198009, A203156, A214321, etc.
        var studentCounter = 0
        
        // Helper function to generate student
        fun createStudent(
            batchPrefix: String,
            matricNum: Int,
            batch: String,
            counter: Int
        ): UKMStudent {
            val matric = "$batchPrefix${String.format("%04d", matricNum)}"
            val password = "UKM$matric" // Password format: UKM + matric number
            val firstName = firstNames[counter % firstNames.size]
            val lastName = lastNames[counter % lastNames.size]
            val fullName = "$firstName $lastName"
            val email = "${matric.lowercase()}@siswa.ukm.edu.my"
            val phone = "01${(2..9).random()}${(1000000..9999999).random()}"
            val faculty = faculties[counter % faculties.size]
            val program = programs[counter % programs.size]
            
            return UKMStudent(
                matricNumber = matric,
                passwordHash = hashPassword(password),
                fullName = fullName,
                email = email,
                phoneNumber = phone,
                faculty = faculty,
                academicProgram = program,
                yearOfStudy = determineYearOfStudy(batch),
                enrolmentLevel = "Sarjana Muda",
                academicStatus = determineAcademicStatus(batch),
                batch = batch
            )
        }
        
        // Generate diverse matric numbers across the full range (0000-9999)
        // Using a mix of sequential and scattered numbers for better coverage
        // Includes specific test cases: A198009, A203156, A214321, etc.
        val matricNumbersA18 = (0..99).toList() + listOf(100, 200, 500, 1000, 2000, 5000, 8000, 8009, 9000, 9999)
        val matricNumbersA19 = (0..99).toList() + listOf(100, 200, 500, 800, 8009, 1234, 5678, 9000, 9999)
        val matricNumbersA20 = (0..99).toList() + listOf(100, 200, 500, 800, 1111, 1234, 2222, 3156, 3333, 3399, 4444, 5000, 5555, 5678, 6000, 7000, 8000, 9000, 9999)
        val matricNumbersA21 = (0..99).toList() + listOf(100, 200, 500, 800, 1111, 1234, 2222, 3333, 3456, 4321, 4444, 5000, 5555, 5678, 6000, 7000, 8000, 9000, 9999)
        val matricNumbersA22 = (0..99).toList() + listOf(100, 200, 500, 800, 1234, 4000, 5000, 6000, 9999)
        
        // Batch A18 (Alumni - Tamat Pengajian)
        for (i in matricNumbersA18) {
            val batch = "A18"
            students.add(createStudent(batch, i, batch, studentCounter))
            studentCounter++
        }
        
        // Batch A19 (4th year - Mendaftar)
        for (i in matricNumbersA19) {
            val batch = "A19"
            students.add(createStudent(batch, i, batch, studentCounter))
            studentCounter++
        }
        
        // Batch A20 (3rd year - Mendaftar) - Eligible for driver
        for (i in matricNumbersA20) {
            val batch = "A20"
            students.add(createStudent(batch, i, batch, studentCounter))
            studentCounter++
        }
        
        // Batch A21 (2nd year - Mendaftar) - Eligible for driver
        for (i in matricNumbersA21) {
            val batch = "A21"
            students.add(createStudent(batch, i, batch, studentCounter))
            studentCounter++
        }
        
        // Batch A22 (1st year - Mendaftar) - NOT eligible for driver
        for (i in matricNumbersA22) {
            val batch = "A22"
            students.add(createStudent(batch, i, batch, studentCounter))
            studentCounter++
        }
        
        return students
    }
}

