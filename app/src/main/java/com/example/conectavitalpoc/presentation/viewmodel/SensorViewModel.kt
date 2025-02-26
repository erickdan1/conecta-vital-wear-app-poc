package com.example.conectavitalpoc.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conectavitalpoc.data.model.SensorData
import com.example.conectavitalpoc.data.repository.SensorRepository
import kotlinx.coroutines.launch

class SensorViewModel(private val repository: SensorRepository) : ViewModel() {

    fun sendSensorData(sensorData: SensorData) {
        viewModelScope.launch {
            try {
                val response = repository.sendSensorData(sensorData)
                if (response.isSuccessful) {
                    // Sucesso
                } else {
                    // Erro na requisição
                }
            } catch (e: Exception) {
                // Tratamento de erro
            }
        }
    }
}
