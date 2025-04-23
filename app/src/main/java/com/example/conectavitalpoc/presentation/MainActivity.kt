package com.example.conectavitalpoc.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.conectavitalpoc.presentation.viewmodel.MainViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.conectavitalpoc.data.local.SecureStorage
import com.example.conectavitalpoc.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    // Launcher para solicitar a permissão BODY_SENSORS
    private val bodySensorsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                this,
                "Permissão BODY_SENSORS é necessária para capturar os dados dos sensores.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Solicita a permissão BODY_SENSORS, se necessário
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BODY_SENSORS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            bodySensorsPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
        }

        // Observa atualizações da frequência cardíaca
        viewModel.heartRate.observe(this) { heartRate ->
            binding.heartRateTextView.text = heartRate?.let {
                "❤️  ${it.toInt()} bpm"
            } ?: "Sem dados"
        }

        // Atualiza o texto do botão conforme o status da medição
        viewModel.measurementStatus.observe(this) { isMeasuring ->
            binding.measureButton.text = if (isMeasuring) "Parar Medição" else "Iniciar Medição"
        }

        // Listener para o toggle do monitoramento passivo
//        binding.passiveMonitoringToggle.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                viewModel.startPassiveMonitoring()
//            } else {
//                viewModel.stopPassiveMonitoring()
//            }
//        }

        // Listener para o botão de medição em tempo real
        binding.measureButton.setOnClickListener {
            if (viewModel.measurementStatus.value == true) {
                viewModel.stopRealTimeMeasurement()
            } else {
                viewModel.startRealTimeMeasurement()
            }
        }

        fun checkTokenStatus() {
            val token = SecureStorage.getAuthToken(this)

            if (token != null) {
                binding.tokenStatusTextView.text = "Autenticado ✅"
            } else {
                binding.tokenStatusTextView.text = "Token Expirado ❌"
            }
        }
        checkTokenStatus()
    }
}