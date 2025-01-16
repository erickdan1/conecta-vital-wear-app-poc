/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.conectavitalpoc.presentation

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveMonitoringClient
import androidx.health.services.client.data.DataType
import androidx.lifecycle.lifecycleScope
import com.example.conectavitalpoc.R
import kotlinx.coroutines.launch
import androidx.concurrent.futures.await
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.MeasureClient
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.unregisterMeasureCallback

class MainActivity : ComponentActivity() {

    // Declaração do cliente para monitoramento passivo
    private lateinit var passiveMonitoringClient: PassiveMonitoringClient

    // Declaração do cliente para medição em tempo real
    private lateinit var measureClient: MeasureClient

    // TextView para exibir os dados do ritmo cardíaco
    private lateinit var heartRateTextView: TextView

    // Switch para ativar/desativar monitoramento passivo
    private lateinit var passiveMonitoringToggle: Switch

    // Button para iniciar/parar a medição em tempo real
    private lateinit var measureButton: Button

    private var isPassiveMonitoringEnabled = false
    private var isMeasuring = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuração inicial da interface
        setContentView(R.layout.activity_main)

        // Obtém as referências para os elementos de interface
        heartRateTextView = findViewById(R.id.heartRateTextView)
        passiveMonitoringToggle = findViewById(R.id.passiveMonitoringToggle)
        measureButton = findViewById(R.id.measureButton)

        // Inicialização dos clientes Health Services
        passiveMonitoringClient = HealthServices.getClient(this).passiveMonitoringClient
        measureClient = HealthServices.getClient(this).measureClient
        Log.d("MainActivity", "Clientes de monitoramento inicializados.")
        Log.d("MainActivity", "isPassiveMonitoringEnabled: $isPassiveMonitoringEnabled")
        Log.d("MainActivity", "isMeasuring: $isMeasuring")

        // Configura o listener do toggle para ativar/desativar monitoramento passivo
        passiveMonitoringToggle.setOnCheckedChangeListener { _, isChecked ->
            isPassiveMonitoringEnabled = isChecked
            if (isChecked) {
                lifecycleScope.launch { setupPassiveMonitoring() }
            } else {
                lifecycleScope.launch { disablePassiveMonitoring() }
            }
        }

        // Configura o listener do botão para iniciar/parar medição em tempo real
        measureButton.setOnClickListener {
            if (isMeasuring) {
                stopRealTimeMeasurement()
            } else {
                startRealTimeMeasurement()
            }
        }
    }

    // Configura o monitoramento de ritmo cardíaco
    @SuppressLint("SetTextI18n")
    private suspend fun setupPassiveMonitoring() {
        try {
            // Verifica se o dispositivo suporta monitoramento passivo de ritmo cardíaco
            val capabilitiesTask = passiveMonitoringClient.getCapabilitiesAsync()
            val capabilities = capabilitiesTask.await()
            if (DataType.HEART_RATE_BPM !in capabilities.supportedDataTypesPassiveMonitoring) {
                Log.w("PassiveMonitoring", "Monitoramento passivo de frequência cardíaca não suportado.")
                heartRateTextView.text = "Monitoramento de ritmo cardíaco não suportado neste dispositivo."
                return
            }

            // Registra o serviço de monitoramento
            val passiveListenerConfig = PassiveListenerConfig(
                dataTypes = setOf(DataType.HEART_RATE_BPM),
                shouldUserActivityInfoBeRequested = false,
                dailyGoals = setOf(),
                healthEventTypes = setOf()
            )

            passiveMonitoringClient.setPassiveListenerServiceAsync(HeartRatePassiveService::class.java, passiveListenerConfig)
            Log.d("PassiveMonitoring", "Monitoramento passivo configurado com sucesso.")
            // Informa ao usuário que o monitoramento foi iniciado
            heartRateTextView.text = "Monitorando ritmo cardíaco..."
        } catch (e: Exception) {
            Log.e("PassiveMonitoring", "Erro ao configurar monitoramento passivo: ${e.message}")
            heartRateTextView.text = "Erro ao configurar monitoramento passivo."
        }
    }

    private fun disablePassiveMonitoring() {
        try {
            passiveMonitoringClient.clearPassiveListenerServiceAsync()
            Log.d("PassiveMonitoring", "Monitoramento passivo desativado com sucesso.")
        } catch (e: Exception) {
            Log.e("PassiveMonitoring", "Erro ao desativar o monitoramento passivo: ${e.message}")
        }
    }

    // Declaração de uma variável para armazenar o callback
    private var measureCallback: MeasureCallback? = null

    // Flag para alternar entre simulação e medição real
    private val isSimulationMode = true

    // Handler e Runnable para simulação
    private val handler = Handler(Looper.getMainLooper())
    private val simulationRunnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            val syntheticHeartRate = (60..100).random() // Gera valor aleatório
            Log.d("Simulation", "Simulação: Ritmo cardíaco gerado: $syntheticHeartRate BPM")
            heartRateTextView.text = "Simulação: Ritmo cardíaco: $syntheticHeartRate BPM"
            handler.postDelayed(this, 1000) // Repete a cada 1 segundo
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startRealTimeMeasurement() {
        lifecycleScope.launch {
            try {
                if (isSimulationMode) {
                    // Inicia a simulação
                    handler.post(simulationRunnable)
                    isMeasuring = true
                    measureButton.text = "Parar Medição"
                    Log.d("Simulation", "Modo de simulação iniciado.")
                    return@launch
                }

                // Verifica as capacidades do dispositivo (apenas para medição real)
                val capabilities = measureClient.getCapabilitiesAsync().await()
                if (DataType.HEART_RATE_BPM !in capabilities.supportedDataTypesMeasure) {
                    Log.w("RealTimeMeasurement", "Medição em tempo real não suportada.")
                    heartRateTextView.text = "Medição em tempo real não suportada."
                    return@launch
                }

                // Cria e armazena o callback
                measureCallback = object : MeasureCallback {
                    override fun onAvailabilityChanged(dataType: DeltaDataType<*, *>, availability: Availability) {
                        if (availability is DataTypeAvailability) {
                            Log.d("RealTimeMeasurement", "Disponibilidade: $availability")
                        }
                    }

                    override fun onDataReceived(data: DataPointContainer) {
                        val heartRateData = data.getData(DataType.HEART_RATE_BPM)
                        if (heartRateData.isNotEmpty()) {
                            val heartRateBpm = heartRateData.first().value
                            Log.d("RealTimeMeasurement", "Frequência cardíaca recebida: $heartRateBpm BPM")
                            heartRateTextView.text = "Ritmo cardíaco: ${heartRateBpm.toInt()} BPM"
                        } else {
                            Log.d("RealTimeMeasurement", "Nenhum dado de frequência cardíaca recebido.")
                        }
                    }
                }

                // Registra o callback
                measureCallback?.let {
                    measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, it)
                }

                // Atualiza o estado e o botão
                isMeasuring = true
                measureButton.text = "Parar Medição"
                Log.d("RealTimeMeasurement", "Medição em tempo real iniciada.")
            } catch (e: Exception) {
                Log.e("RealTimeMeasurement", "Erro ao iniciar medição: ${e.message}")
                heartRateTextView.text = "Erro ao iniciar medição."
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun stopRealTimeMeasurement() {
        lifecycleScope.launch {
            try {
                if (isSimulationMode) {
                    // Para a simulação
                    handler.removeCallbacks(simulationRunnable)
                    // heartRateTextView.text = "Simulação parada."
                    Log.d("Simulation", "Modo de simulação parado.")
                } else {
                    // Desregistra o callback de medição real
                    measureCallback?.let {
                        measureClient.unregisterMeasureCallback(DataType.HEART_RATE_BPM, it)
                    }
                    measureCallback = null // Libera a variável
                    // heartRateTextView.text = "Medição em tempo real parada."
                    Log.d("RealTimeMeasurement", "Medição em tempo real parada.")
                }
            } catch (e: Exception) {
                Log.e("RealTimeMeasurement", "Erro ao parar medição: ${e.message}")
            } finally {
                // Atualiza o estado e o botão
                isMeasuring = false
                measureButton.text = "Iniciar Medição"
            }
        }
    }

    // Serviço interno para processamento passivo de dados
    class HeartRatePassiveService : PassiveListenerService() {
        override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
            val heartRateData = dataPoints.getData(DataType.HEART_RATE_BPM)
            if (heartRateData.isNotEmpty()) {
                val heartRate = heartRateData.first().value
                Log.d("HeartRatePassiveService", "Ritmo cardíaco recebido: $heartRate BPM")

                // Enviar dados para a UI via Broadcast
                val intent = Intent("com.example.HEART_RATE_UPDATE")
                intent.putExtra("heart_rate", heartRate)
                sendBroadcast(intent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        // Registrar um receiver para atualizar a UI com os dados do serviço passivo
        val filter = IntentFilter("com.example.HEART_RATE_UPDATE")
        registerReceiver(heartRateReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(heartRateReceiver)
    }

    private val heartRateReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            val heartRate = intent.getDoubleExtra("heart_rate", -1.0)
            if (heartRate >= 0) {
                heartRateTextView.text = "Ritmo cardíaco (passivo): ${heartRate.toInt()} BPM"
            }
        }
    }
}
