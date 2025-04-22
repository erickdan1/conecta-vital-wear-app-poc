package com.example.conectavitalpoc.domain

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class TokenReceiverService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (path == "/auth-token") {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val token = dataMap.getString("token")
                    token?.let {
                        Log.d("TokenReceiverService", "Token recebido: $it")
                        // Salve o token de forma segura (veja o próximo passo)
                        saveToken(it)
                    }
                }
            }
        }
    }

    private fun saveToken(token: String) {
        val prefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("auth_token", token).apply()
    }
}