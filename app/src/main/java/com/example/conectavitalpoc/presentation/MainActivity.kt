/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.conectavitalpoc.presentation

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveMonitoringClient
import androidx.health.services.client.data.DataType
import androidx.lifecycle.lifecycleScope
import com.example.conectavitalpoc.R
import kotlinx.coroutines.launch
import androidx.concurrent.futures.await
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer

class MainActivity : ComponentActivity() {

    // Declaração do cliente para monitoramento passivo
    private lateinit var passiveMonitoringClient: PassiveMonitoringClient

    // TextView para exibir os dados do ritmo cardíaco
    private lateinit var heartRateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuração inicial da interface
        setContentView(R.layout.activity_main)
        heartRateTextView = findViewById(R.id.heartRateTextView)

        // Inicialização do cliente Health Services
        passiveMonitoringClient = HealthServices.getClient(this).passiveMonitoringClient

        // Verifica as capacidades do dispositivo e inicia o monitoramento
        lifecycleScope.launch {
            setupHeartRateMonitoring()
        }
    }

    // Configura o monitoramento de ritmo cardíaco
    private suspend fun setupHeartRateMonitoring() {
        // Verifica se o dispositivo suporta monitoramento passivo de ritmo cardíaco
        val capabilitiesTask = passiveMonitoringClient.getCapabilitiesAsync()
        val capabilities = capabilitiesTask.await()
        if (DataType.HEART_RATE_BPM !in capabilities.supportedDataTypesPassiveMonitoring) {
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
        //val config = PassiveMonitoringClient.PassiveListenerConfig.builder()
        //    .setDataTypes(setOf(DataType.HEART_RATE_BPM))
        //    .build()

        passiveMonitoringClient.setPassiveListenerServiceAsync(HeartRatePassiveService::class.java, passiveListenerConfig)

        // Informa ao usuário que o monitoramento foi iniciado
        heartRateTextView.text = "Monitorando ritmo cardíaco..."
    }

    // Serviço interno para processar os dados recebidos
    class HeartRatePassiveService : PassiveListenerService() {
        override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
            val heartRateData = dataPoints.getData(DataType.HEART_RATE_BPM)
            heartRateData.forEach { dataPoint ->
                Log.d("HeartRate", "Dados recebidos: ${dataPoint.value} BPM")
                val heartRate = dataPoint.value
                // Chama o método para atualizar a UI passando o contexto principal
                val mainContext = applicationContext as MainActivity
                mainContext.runOnUiThread {
                    mainContext.heartRateTextView.text = "Ritmo cardíaco: $heartRate BPM"
                }
            }
        }
    }
}
