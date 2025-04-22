package com.example.conectavitalpoc.utils

import android.util.Base64
import org.json.JSONObject

object TokenUtils {
    // Retorna o tempo de expiração do token em milissegundos
    fun extractExpirationFromJWT(token: String): Long? {
        return try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                val json = JSONObject(payload)
                val expInSeconds = json.getLong("exp")
                expInSeconds * 1000 // converte para milissegundos
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // Verifica se o token está expirado com base no horário atual
    fun isTokenExpired(token: String): Boolean {
        val expirationTime = extractExpirationFromJWT(token)
        return expirationTime != null && System.currentTimeMillis() > expirationTime
    }
}