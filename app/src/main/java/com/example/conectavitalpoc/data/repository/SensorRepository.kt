package com.example.conectavitalpoc.data.repository

import com.example.conectavitalpoc.data.model.SensorData
import com.example.conectavitalpoc.data.remote.RetrofitInstance

class SensorRepository {
    private val api = RetrofitInstance.sensorApi

    suspend fun sendSensorData(sensorData: SensorData) = api.sendSensorData(sensorData)
}