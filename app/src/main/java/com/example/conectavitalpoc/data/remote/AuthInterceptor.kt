package com.example.conectavitalpoc.data.remote

import android.content.Context
import com.example.conectavitalpoc.data.local.SecureStorage
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(private val context: Context) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = SecureStorage.getAuthToken(context) // Recupera o token armazenado

        val request: Request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token") // Adiciona o token no cabeçalho
                .build()
        } else {
            chain.request() // Segue sem adicionar o cabeçalho caso não haja token
        }

        return chain.proceed(request)
    }
}