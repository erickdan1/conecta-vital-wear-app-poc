package com.example.conectavitalpoc.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // URL base do seu backend (usar HTTPS)
    private const val BASE_URL = "http://10.0.2.2:3000/"

    // Instância do Retrofit configurada com GsonConverter
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Cria a instância da interface SensorApi
    val sensorApi: SensorApi by lazy {
        retrofit.create(SensorApi::class.java)
    }
}