package com.yldefonso.ojodchakra.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import androidx.core.util.Consumer

/** Proveedor de ubicación que se intentará. */
enum class ProveedorUbicacion { GPS, RED }

/** Resultado de pedir la ubicación actual. */
sealed interface ResultadoUbicacion

/**
 * Posición encontrada.
 * [antiguedadMin] es null si se midió en este momento; si no, es la edad de la última posición guardada por el sistema.
 */
data class UbicacionEncontrada(
    val latitud: Double,
    val longitud: Double,
    val precisionMetros: Float?,
    val esGps: Boolean,
    val antiguedadMin: Int? = null
) : ResultadoUbicacion

enum class FalloUbicacion(val mensaje: String) : ResultadoUbicacion {
    SIN_PERMISO(
        "Sin permiso de ubicación. Concédelo en Ajustes del celular o escribe las coordenadas a mano."
    ),
    UBICACION_DESACTIVADA(
        "La ubicación del celular está apagada. Actívala desde el panel rápido y vuelve a intentar."
    ),
    SIN_PRECISION(
        "Para usar el GPS concede ubicación precisa en Ajustes de la app, o activa la ubicación de red."
    ),
    SIN_SENAL(
        "No se obtuvo señal de ubicación. Sal a un lugar abierto, espera un minuto y reintenta, o escribe las coordenadas a mano."
    )
}

/** Estado de los proveedores, para mostrarlo en pantalla y ayudar a probar. */
data class EstadoUbicacion(
    val hayInternet: Boolean,
    val gpsActivo: Boolean,
    val redActiva: Boolean
) {
    fun textoProveedores(): String =
        "Internet: ${si(hayInternet)} · GPS: ${si(gpsActivo)} · Red: ${si(redActiva)}"

    fun textoAyuda(): String = when {
        !hayInternet && !gpsActivo -> "Sin internet solo funciona el GPS: actívalo y sal a un lugar abierto."
        !hayInternet -> "Sin internet se usa solo el GPS: sal a un lugar abierto y espera hasta un minuto."
        redActiva && gpsActivo -> "Con internet se intenta primero la red y luego el GPS."
        redActiva -> "Con internet se usa la red, porque el GPS está apagado."
        gpsActivo -> "Con internet pero sin red de ubicación se usa el GPS."
        else -> "Activa el GPS o la ubicación de red para usar esta función."
    }

    private fun si(valor: Boolean): String = if (valor) "sí" else "no"
}

/** Lógica pura de elección de proveedores (testeable sin Android). */
object ResolutorProveedorUbicacion {

    /**
     * Con internet: primero la red (rápida) y luego el GPS (más preciso).
     * Sin internet: solo GPS, porque la red de ubicación necesita datos o Wi‑Fi.
     */
    fun candidatos(
        finePermitido: Boolean,
        gpsActivo: Boolean,
        redActiva: Boolean,
        hayInternet: Boolean
    ): List<ProveedorUbicacion> = buildList {
        val gpsUsable = finePermitido && gpsActivo
        if (hayInternet) {
            if (redActiva) add(ProveedorUbicacion.RED)
            if (gpsUsable) add(ProveedorUbicacion.GPS)
        } else if (gpsUsable) {
            add(ProveedorUbicacion.GPS)
        }
    }

    /** Sin internet el GPS tarda más en el primer fix, porque no puede descargar datos de ayuda. */
    fun tiempoEsperaMs(proveedor: ProveedorUbicacion, hayInternet: Boolean): Long = when {
        proveedor == ProveedorUbicacion.RED -> 10_000L
        hayInternet -> 30_000L
        else -> 60_000L
    }
}

object UbicacionHelper {

    private const val TAG = "UbicacionHelper"
    private const val EDAD_MAXIMA_ULTIMA_MS = 10L * 60L * 1000L

    /** Permiso de ubicación precisa o aproximada concedido. */
    fun tienePermiso(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    /** Hay conexión a internet validada en este momento. */
    @SuppressLint("MissingPermission")
    fun hayInternet(context: Context): Boolean {
        val gestor = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activa = gestor.activeNetwork ?: return false
        val capacidades = gestor.getNetworkCapabilities(activa) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /** Estado actual de internet, GPS y red de ubicación. */
    fun estado(context: Context): EstadoUbicacion {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return EstadoUbicacion(
            hayInternet = hayInternet(context),
            gpsActivo = manager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true,
            redActiva = manager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
        )
    }

    /** Devuelve la ubicación actual o un motivo claro de fallo. Siempre se llama en el hilo principal. */
    @SuppressLint("MissingPermission")
    fun obtener(context: Context, onResultado: (ResultadoUbicacion) -> Unit) {
        if (!tienePermiso(context)) {
            onResultado(FalloUbicacion.SIN_PERMISO)
            return
        }

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (manager == null) {
            onResultado(FalloUbicacion.UBICACION_DESACTIVADA)
            return
        }

        val finePermitido = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val estado = estado(context)

        val candidatos = ResolutorProveedorUbicacion.candidatos(
            finePermitido, estado.gpsActivo, estado.redActiva, estado.hayInternet
        )
        Log.d(TAG, "pedir ubicación: ${estado.textoProveedores()} · candidatos=$candidatos")

        if (candidatos.isEmpty()) {
            onResultado(
                if (!estado.gpsActivo && !estado.redActiva) FalloUbicacion.UBICACION_DESACTIVADA
                else if (!estado.hayInternet && !estado.gpsActivo) FalloUbicacion.UBICACION_DESACTIVADA
                else FalloUbicacion.SIN_PRECISION
            )
            return
        }

        intentar(context, manager, candidatos, 0, estado.hayInternet, onResultado)
    }

    /** Prueba cada proveedor con su tiempo límite; si ninguno responde, usa la última posición reciente. */
    @SuppressLint("MissingPermission")
    private fun intentar(
        context: Context,
        manager: LocationManager,
        candidatos: List<ProveedorUbicacion>,
        indice: Int,
        hayInternet: Boolean,
        onResultado: (ResultadoUbicacion) -> Unit
    ) {
        if (indice >= candidatos.size) {
            val ultima = ultimaConocida(manager, candidatos)
            Log.d(TAG, "sin respuesta de ${candidatos.size} proveedor(es); última guardada=${ultima != null}")
            onResultado(
                ultima?.aResultado(antiguedadMin = antiguedadEnMinutos(ultima))
                    ?: FalloUbicacion.SIN_SENAL
            )
            return
        }

        val handler = Handler(Looper.getMainLooper())
        val cancelacion = CancellationSignal()
        var resuelto = false
        val proveedor = candidatos[indice]

        fun siguiente(ubicacion: Location?) {
            if (resuelto) return
            resuelto = true
            handler.removeCallbacksAndMessages(null)
            if (ubicacion != null) {
                Log.d(TAG, "posición actual por ${proveedor.name}")
                onResultado(ubicacion.aResultado())
            } else {
                Log.d(TAG, "${proveedor.name} no respondió a tiempo")
                intentar(context, manager, candidatos, indice + 1, hayInternet, onResultado)
            }
        }

        handler.postDelayed({
            if (!resuelto) {
                cancelacion.cancel()
                siguiente(null)
            }
        }, ResolutorProveedorUbicacion.tiempoEsperaMs(proveedor, hayInternet))

        try {
            val consumer = Consumer<Location?> { ubicacion -> siguiente(ubicacion) }
            LocationManagerCompat.getCurrentLocation(
                manager,
                nombreProveedor(proveedor),
                cancelacion,
                ContextCompat.getMainExecutor(context),
                consumer
            )
        } catch (e: SecurityException) {
            siguiente(null)
        } catch (e: Exception) {
            siguiente(null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun ultimaConocida(
        manager: LocationManager,
        candidatos: List<ProveedorUbicacion>
    ): Location? {
        val ahora = SystemClock.elapsedRealtimeNanos()
        return candidatos
            .mapNotNull { proveedor ->
                try {
                    manager.getLastKnownLocation(nombreProveedor(proveedor))
                } catch (e: SecurityException) {
                    null
                }
            }
            .filter { ahora - it.elapsedRealtimeNanos <= EDAD_MAXIMA_ULTIMA_MS * 1_000_000L }
            .maxByOrNull { it.time }
    }

    private fun antiguedadEnMinutos(ubicacion: Location): Int {
        val ms = (SystemClock.elapsedRealtimeNanos() - ubicacion.elapsedRealtimeNanos) / 1_000_000L
        return (ms / 60_000L).toInt()
    }

    private fun Location.aResultado(antiguedadMin: Int? = null): UbicacionEncontrada = UbicacionEncontrada(
        latitud = latitude,
        longitud = longitude,
        precisionMetros = if (hasAccuracy()) accuracy else null,
        esGps = provider == LocationManager.GPS_PROVIDER,
        antiguedadMin = antiguedadMin
    )

    private fun nombreProveedor(proveedor: ProveedorUbicacion): String = when (proveedor) {
        ProveedorUbicacion.GPS -> LocationManager.GPS_PROVIDER
        ProveedorUbicacion.RED -> LocationManager.NETWORK_PROVIDER
    }
}
