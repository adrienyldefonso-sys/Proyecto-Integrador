package com.yldefonso.ojodchakra.screens.parcelas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yldefonso.ojodchakra.repository.ParcelaRepository
import com.yldefonso.ojodchakra.util.ErroresParcela
import com.yldefonso.ojodchakra.util.ValidadorParcela
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class ParcelaFormState(
    val nombre: String = "",
    val cultivo: String = "Papa",
    val referencia: String = "",
    val latitud: String = "",
    val longitud: String = "",
    val errores: ErroresParcela = ErroresParcela(),
    val buscandoUbicacion: Boolean = false,
    val guardando: Boolean = false,
    val mensaje: String? = null
)

class ParcelaFormViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ParcelaRepository(application)

    private val _estado = MutableStateFlow(ParcelaFormState())
    val estado: StateFlow<ParcelaFormState> = _estado.asStateFlow()

    fun onNombreChange(valor: String) = _estado.update {
        it.copy(nombre = valor.take(100), errores = it.errores.copy(nombre = null), mensaje = null)
    }

    fun onCultivoChange(valor: String) = _estado.update {
        it.copy(cultivo = valor.take(50), errores = it.errores.copy(cultivo = null), mensaje = null)
    }

    fun onReferenciaChange(valor: String) = _estado.update {
        it.copy(referencia = valor.take(150), mensaje = null)
    }

    fun onLatitudChange(valor: String) = _estado.update {
        it.copy(latitud = valor, errores = it.errores.copy(latitud = null), mensaje = null)
    }

    fun onLongitudChange(valor: String) = _estado.update {
        it.copy(longitud = valor, errores = it.errores.copy(longitud = null), mensaje = null)
    }

    fun onBuscandoUbicacion() = _estado.update {
        it.copy(buscandoUbicacion = true, mensaje = null)
    }

    fun onUbicacionObtenida(latitud: Double?, longitud: Double?) = _estado.update {
        if (latitud == null || longitud == null) {
            it.copy(
                buscandoUbicacion = false,
                mensaje = "No se pudo obtener la ubicación. Activa el GPS o escribe las coordenadas a mano."
            )
        } else {
            it.copy(
                buscandoUbicacion = false,
                latitud = String.format(Locale.US, "%.6f", latitud),
                longitud = String.format(Locale.US, "%.6f", longitud),
                errores = it.errores.copy(latitud = null, longitud = null),
                mensaje = null
            )
        }
    }

    fun mostrarMensaje(texto: String) = _estado.update {
        it.copy(buscandoUbicacion = false, mensaje = texto)
    }

    fun guardar(onGuardada: () -> Unit) {
        val actual = _estado.value
        val errores = ValidadorParcela.validar(
            actual.nombre, actual.cultivo, actual.latitud, actual.longitud
        )

        if (errores.hayErrores) {
            _estado.update { it.copy(errores = errores, mensaje = "Revisa los campos marcados.") }
            return
        }

        val lat = ValidadorParcela.parsearCoordenada(actual.latitud) ?: return
        val lon = ValidadorParcela.parsearCoordenada(actual.longitud) ?: return

        _estado.update { it.copy(guardando = true, mensaje = null) }

        viewModelScope.launch {
            try {
                repository.registrar(
                    nombre = actual.nombre,
                    cultivo = actual.cultivo,
                    referencia = actual.referencia,
                    latitud = lat,
                    longitud = lon
                )
                _estado.update { it.copy(guardando = false) }
                onGuardada()
            } catch (e: Exception) {
                _estado.update {
                    it.copy(guardando = false, mensaje = "No se pudo guardar la parcela. Intenta de nuevo.")
                }
            }
        }
    }
}