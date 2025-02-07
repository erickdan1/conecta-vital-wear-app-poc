package com.example.conectavitalpoc.data.remote

import com.example.conectavitalpoc.data.model.SensorData

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SensorApi {
    @POST("/api/sensordata")
    suspend fun sendSensorData(@Body sensorData: SensorData): Response<Unit>
}
