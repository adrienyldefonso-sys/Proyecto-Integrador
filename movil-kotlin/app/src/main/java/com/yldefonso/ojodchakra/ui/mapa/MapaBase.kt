package com.yldefonso.ojodchakra.ui.mapa

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.yldefonso.ojodchakra.R
import com.yldefonso.ojodchakra.util.SeveridadMapa

/** Punto de referencia (Lima) cuando todavía no hay coordenadas que mostrar. */
val CENTRO_PREDETERMINADO = LatLng(-12.0464, -77.0428)

/** Marcador genérico: no depende de Room ni de la pantalla que lo use. */
data class MarcadorMapa(
    val id: String,
    val posicion: LatLng,
    val titulo: String,
    val hue: Float = SeveridadMapa.HUE_VERDE
)

/**
 * Mapa reutilizable. Solo se usa con internet: Google Maps necesita descargar las imágenes del mapa.
 *
 * Encuadre:
 * - [enfocar] tiene prioridad: centra en ese punto con zoom cercano.
 * - Si no, se encuadran todos los marcadores para ver todas las parcelas.
 * - En modo oscuro el mapa usa un estilo oscuro acorde con la app.
 */
@Composable
fun MapaBase(
    centro: LatLng,
    zoom: Float,
    marcadores: List<MarcadorMapa>,
    modifier: Modifier = Modifier,
    enfocar: LatLng? = null,
    onMapaTocado: ((LatLng) -> Unit)? = null,
    onMarcadorTocado: ((String) -> Unit)? = null
) {
    val contexto = LocalContext.current
    val oscuro = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val propiedades = remember(oscuro) {
        MapProperties(
            mapStyleOptions = if (oscuro) {
                MapStyleOptions.loadRawResourceStyle(contexto, R.raw.mapa_oscuro)
            } else {
                null
            }
        )
    }

    val camara = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centro, zoom)
    }
    var mapaListo by remember { mutableStateOf(false) }

    LaunchedEffect(mapaListo, marcadores, enfocar) {
        if (!mapaListo) return@LaunchedEffect
        try {
            when {
                enfocar != null -> camara.move(CameraUpdateFactory.newLatLngZoom(enfocar, 16f))
                marcadores.size == 1 -> camara.move(
                    CameraUpdateFactory.newLatLngZoom(marcadores.first().posicion, 15f)
                )
                marcadores.size > 1 -> {
                    val limites = LatLngBounds.builder().apply {
                        marcadores.forEach { include(it.posicion) }
                    }.build()
                    camara.move(CameraUpdateFactory.newLatLngBounds(limites, 120))
                }
            }
        } catch (e: IllegalStateException) {
            // El mapa todavía no tiene tamaño: el siguiente cambio volverá a encuadrar
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = camara,
        properties = propiedades,
        uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false),
        onMapLoaded = { mapaListo = true },
        onMapClick = onMapaTocado
    ) {
        marcadores.forEach { marcador ->
            key(marcador.id, marcador.posicion.latitude, marcador.posicion.longitude) {
                Marker(
                    state = rememberMarkerState(key = marcador.id, position = marcador.posicion),
                    title = marcador.titulo,
                    icon = BitmapDescriptorFactory.defaultMarker(marcador.hue),
                    onClick = { _ ->
                        onMarcadorTocado?.invoke(marcador.id)
                        false
                    }
                )
            }
        }
    }
}

/**
 * Selector para el formulario: al tocar el mapa se fija el punto de la parcela.
 * Si ya hay coordenadas, se muestra el marcador y la cámara se centra en él.
 */
@Composable
fun SelectorUbicacionMapa(
    latitud: Double?,
    longitud: Double?,
    onUbicacionElegida: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    val tieneUbicacion = latitud != null && longitud != null
    val punto = if (latitud != null && longitud != null) LatLng(latitud, longitud) else null
    val marcadores = if (punto != null) {
        listOf(MarcadorMapa(id = "seleccion", posicion = punto, titulo = "Parcela"))
    } else {
        emptyList()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        MapaBase(
            centro = punto ?: CENTRO_PREDETERMINADO,
            zoom = if (tieneUbicacion) 16f else 12f,
            marcadores = marcadores,
            modifier = Modifier.fillMaxSize(),
            enfocar = punto,
            onMapaTocado = onUbicacionElegida
        )
    }
}
