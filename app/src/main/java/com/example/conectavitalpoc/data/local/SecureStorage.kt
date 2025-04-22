package com.example.conectavitalpoc.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.conectavitalpoc.utils.TokenUtils

object SecureStorage {
    private const val PREFS_NAME = "auth_prefs"
    private const val HEART_RATE_KEY = "heart_rate"
    private const val AUTH_TOKEN_KEY = "auth_token"

    private fun getEncryptedSharedPreferences(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    fun saveHeartRate(context: Context, heartRate: Double) {
        getEncryptedSharedPreferences(context).edit()
            .putString(HEART_RATE_KEY, heartRate.toString())
            .apply()
    }

    fun getHeartRate(context: Context): Double? {
        return getEncryptedSharedPreferences(context)
            .getString(HEART_RATE_KEY, null)
            ?.toDoubleOrNull()
    }

    // Salvar o token
    fun saveAuthToken(context: Context, token: String) {
        getEncryptedSharedPreferences(context).edit()
            .putString(AUTH_TOKEN_KEY, token)
            .apply()
    }

    // Recuperar o token se ainda estiver válido
    fun getAuthToken(context: Context): String? {
        val sharedPreferences = getEncryptedSharedPreferences(context)
        val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)

        return if (token != null && !TokenUtils.isTokenExpired(token)) {
            token
        } else {
            null // Token expirado ou não existente
        }
    }

    // Limpar token
    fun clearAuthToken(context: Context) {
        getEncryptedSharedPreferences(context).edit()
            .remove(AUTH_TOKEN_KEY)
            .apply()
    }
}