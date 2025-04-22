package com.example.conectavitalpoc.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // URL base do seu backend (usar HTTPS)
    private const val BASE_URL = "http://10.0.2.2:3000/"

    // Método para criar um cliente OkHttp com o Interceptor
    private fun createOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context)) // Usa o interceptor externo
            .build()
    }

    // Método para criar uma instância do Retrofit
    private fun createRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(createOkHttpClient(context)) // Usa o cliente com o interceptor
            .build()
    }

    // Método para criar a instância da API
    fun createSensorApi(context: Context): SensorApi {
        return createRetrofit(context).create(SensorApi::class.java)
    }
}