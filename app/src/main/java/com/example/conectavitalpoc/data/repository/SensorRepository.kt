package com.example.conectavitalpoc.data.repository

import com.example.conectavitalpoc.data.local.SecureStorage
import com.example.conectavitalpoc.data.model.SensorData
import com.example.conectavitalpoc.data.remote.RetrofitInstance
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("America/Sao_Paulo") // Altere para o fuso desejado, como "America/Sao_Paulo"
    return format.format(date)
}

class SensorRepository(private val context: Context) {
    private val api = RetrofitInstance.createSensorApi(context)

    private fun saveHeartRateLocally(heartRate: Double) {
        SecureStorage.saveHeartRate(context, heartRate)
    }

    suspend fun sendSensorData(heartRate: Double): Boolean = withContext(Dispatchers.IO) {
        saveHeartRateLocally(heartRate)
        val sensorData = SensorData(
            heartRate = heartRate,
            registrationDate = formatTimestamp(System.currentTimeMillis())
        )
        return@withContext try {
            val response = api.sendSensorData(sensorData)
            if (!response.isSuccessful) {
                Log.e("Retrofit", "Erro ao enviar dados. Código HTTP: ${response.code()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("Retrofit", "Exceção ao enviar dados: ${e.message}")
            false
        }
    }
}