package com.yldefonso.ojodchakra.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import androidx.core.util.Consumer

object UbicacionHelper {

    // Devuelve la ubicación actual, o null si no hay GPS activo o falla la lectura
    @SuppressLint("MissingPermission")
    fun obtener(context: Context, onResultado: (Location?) -> Unit) {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        if (manager == null) {
            onResultado(null)
            return
        }

        val proveedor = when {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> {
                onResultado(null)
                return
            }
        }

        try {
            // Tipos explícitos para resolver la ambigüedad de sobrecarga
            val signal: CancellationSignal? = null
            val consumer = Consumer<Location?> { ubicacion ->
                onResultado(ubicacion)
            }

            LocationManagerCompat.getCurrentLocation(
                manager,
                proveedor,
                signal,
                ContextCompat.getMainExecutor(context),
                consumer
            )
        } catch (e: Exception) {
            onResultado(null)
        }
    }
}