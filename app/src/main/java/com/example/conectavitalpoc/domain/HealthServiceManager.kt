package com.example.conectavitalpoc.domain

import android.content.Context
import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.Availability
import androidx.health.services.client.unregisterMeasureCallback
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import com.example.conectavitalpoc.data.repository.SensorRepository

import kotlinx.coroutines.*

class HealthServiceManager(private val context: Context, private val sensorRepository: SensorRepository) {

    private val passiveMonitoringClient = HealthServices.getClient(context).passiveMonitoringClient
    private val measureClient = HealthServices.getClient(context).measureClient

    private var measureCallback: MeasureCallback? = null
    private var measurementJob: Job? = null

    suspend fun setupPassiveMonitoring(
        onData: (Double?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val capabilities = passiveMonitoringClient.getCapabilitiesAsync().await()
            if (DataType.HEART_RATE_BPM !in capabilities.supportedDataTypesPassiveMonitoring) {
                onData(null)
                return
            }

            val config = PassiveListenerConfig(
                dataTypes = setOf(DataType.HEART_RATE_BPM),
                shouldUserActivityInfoBeRequested = false,
                dailyGoals = setOf(),
                healthEventTypes = setOf()
            )

            // Registra o serviço com um callback customizado
            passiveMonitoringClient.setPassiveListenerServiceAsync(
                HeartRatePassiveServiceImpl(onData)::class.java,
                config
            )
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun disablePassiveMonitoring() {
        try {
            passiveMonitoringClient.clearPassiveListenerServiceAsync()
        } catch (e: Exception) {
            // Tratar o erro se necessário
        }
    }

    suspend fun startRealTimeMeasurement(
        onData: (Double) -> Unit,
        onMeasurementStarted: () -> Unit,
        onMeasurementStopped: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val capabilities = measureClient.getCapabilitiesAsync().await()
            if (DataType.HEART_RATE_BPM !in capabilities.supportedDataTypesMeasure) {
                onError(Exception("Medição em tempo real não suportada."))
                return
            }

            // Variável local para armazenar a última medição
            var lastHeartRateMeasurement: Double? = null

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
                        lastHeartRateMeasurement = heartRateBpm  // Atualiza a última medição
                        onData(heartRateBpm) // Invoca o callback onData com o valor da medição
                        Log.d("RealTimeMeasurement", "Frequência cardíaca recebida: $heartRateBpm BPM")
                    } else {
                        Log.d("RealTimeMeasurement", "Nenhum dado de frequência cardíaca recebido.")
                    }
                }
            }

            measureCallback?.let {
                measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, it)
            }
            onMeasurementStarted()

            Log.d("RealTimeMeasurement", "Medição em tempo real iniciada.")

            // Após 5 segundos, interrompe a medição
            measurementJob = CoroutineScope(Dispatchers.IO).launch {
                delay(5000L)

                measureCallback?.let {
                    measureClient.unregisterMeasureCallback(DataType.HEART_RATE_BPM, it)
                }

                // Enviar os dados utilizando o sensorRepository
                lastHeartRateMeasurement?.let { heartRateValue ->
                    // Envia os dados utilizando o SensorRepository (que salva localmente e envia via Retrofit)
                    sensorRepository.sendSensorData(heartRateValue)
                    Log.d("RealTimeMeasurement", "Enviando medição: $heartRateValue BPM")
                }
                onMeasurementStopped()
                stopRealTimeMeasurement()
            }
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun stopRealTimeMeasurement() {
        try {
            measureCallback = null  // Libera a variável
            // Cancela o job de envio, se ativo
            measurementJob?.cancel()
            measurementJob = null
            Log.d("RealTimeMeasurement", "Medição em tempo real parada.")
        } catch (e: Exception) {
            Log.e("RealTimeMeasurement", "Erro ao parar medição: ${e.message}")
        }
    }

    // Implementação customizada do serviço passivo
    class HeartRatePassiveServiceImpl(private val onData: (Double?) -> Unit) : PassiveListenerService() {
        override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
            val heartRateData = dataPoints.getData(DataType.HEART_RATE_BPM)
            if (heartRateData.isNotEmpty()) {
                val heartRate = heartRateData.first().value
                Log.d("HeartRatePassiveService", "Ritmo cardíaco recebido: $heartRate BPM")
                onData(heartRate)
            }
        }
    }
}