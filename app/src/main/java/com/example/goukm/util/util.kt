package com.example.goukm.util // Or a suitable package

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SessionManager(context: Context) {

    private var prefs: SharedPreferences

    companion object {
        const val USER_TOKEN = "user_token"
        const val ACTIVE_ROLE = "user_role"
        const val USER_NAME = "user_name"
        const val USER_MATRIC = "user_matric"
        // Add other user details you want to save
    }

    init {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        prefs = EncryptedSharedPreferences.create(
            "secret_shared_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Function to save the authentication token
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    /**
     * Function to fetch the authentication token
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun saveActiveRole(role: String) {
        prefs.edit().putString("user_role", role).apply()
    }

    fun fetchActiveRole(): String? {
        return prefs.getString("user_role", null)
    }

    /**
     * Function to clear all session data
     */
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }


}