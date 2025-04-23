package com.example.conectavitalpoc.presentation

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.conectavitalpoc.R
import com.example.conectavitalpoc.data.local.SecureStorage

class PairingActivity : AppCompatActivity() {

    private lateinit var connectButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pairing)

        connectButton = findViewById(R.id.connectButton)
        Log.d("PairingActivity", "onCreate - Tela carregada")

        connectButton.setOnClickListener {
            Log.d("PairingActivity", "Botão Conectar clicado")

            // Simular recebimento de JWT (depois virá via Bluetooth)
            simulateTokenReception()
        }

        // Verifica se já tem token válido
        val existingToken = SecureStorage.getAuthToken(this)
        if (existingToken != null) {
            Log.d("PairingActivity", "Token já salvo e válido. Iniciando MainActivity diretamente.")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            Log.d("PairingActivity", "Nenhum token válido encontrado. Aguardando pareamento.")
        }
    }

    private fun simulateTokenReception() {
        Handler(Looper.getMainLooper()).postDelayed({
            val fakeToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkVyaWNrIiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE5MDAwMDAwMDB9.\n" // substitua por um válido

            // Salva o token
            SecureStorage.saveAuthToken(this, fakeToken)
            Log.d("PairingActivity", "Token simulado salvo.")

            // Revalida o token
            val token = SecureStorage.getAuthToken(this)
            Log.d("PairingActivity", "Token recuperado: $token")
            if (token != null) {
                Log.d("PairingActivity", "Token é válido. Navegando para MainActivity.")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Log.w("PairingActivity", "Token inválido após salvamento.")
            }

        }, 2000) // Delay para simular tempo de pareamento
    }
}
