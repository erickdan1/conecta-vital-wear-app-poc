package com.example.conectavitalpoc.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conectavitalpoc.data.repository.SensorRepository
import com.example.conectavitalpoc.domain.HealthServiceManager
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorRepository = SensorRepository(application)
    private val healthServiceManager = HealthServiceManager(application, sensorRepository)

    private val _heartRate = MutableLiveData<Double?>()
    val heartRate: LiveData<Double?> = _heartRate

    private val _measurementStatus = MutableLiveData<Boolean>(false)
    val measurementStatus: LiveData<Boolean> = _measurementStatus

//    fun startPassiveMonitoring() {
//        viewModelScope.launch {
//            healthServiceManager.setupPassiveMonitoring(
//                onData = { hr -> _heartRate.postValue(hr) },
//                onError = { /* Tratar o erro se necessário */ }
//            )
//        }
//    }
//
//    fun stopPassiveMonitoring() {
//        healthServiceManager.disablePassiveMonitoring()
//    }

    fun startRealTimeMeasurement() {
        viewModelScope.launch {
            healthServiceManager.startRealTimeMeasurement(
                onData = { hr -> _heartRate.postValue(hr) },
                onMeasurementStarted = { _measurementStatus.postValue(true) },
                onMeasurementStopped = { _measurementStatus.postValue(false) },
                onError = { /* Tratar o erro se necessário */ }
            )
        }
    }

    fun stopRealTimeMeasurement() {
        viewModelScope.launch {
            healthServiceManager.stopRealTimeMeasurement()
            _measurementStatus.postValue(false)
        }
    }
}