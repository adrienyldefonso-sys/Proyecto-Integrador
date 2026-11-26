package com.yldefonso.ojodchakra.ui.mapa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.yldefonso.ojodchakra.data.model.ParcelaEnMapa
import com.yldefonso.ojodchakra.util.SeveridadMapa

/** Convierte las parcelas con coordenadas en marcadores, coloreados por severidad. */
fun marcadoresDe(parcelas: List<ParcelaEnMapa>): List<MarcadorMapa> =
    parcelas.mapNotNull { parcela ->
        val lat = parcela.latitud
        val lon = parcela.longitud
        if (lat == null || lon == null) {
            null
        } else {
            MarcadorMapa(
                id = parcela.id,
                posicion = LatLng(lat, lon),
                titulo = parcela.nombre,
                hue = SeveridadMapa.hue(parcela.severidad)
            )
        }
    }

/** Leyenda de colores del mapa. */
@Composable
fun LeyendaSeveridad(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ElementoLeyenda(texto = "Alta", color = MapaColores.Alta)
        ElementoLeyenda(texto = "Media", color = MapaColores.Media)
        ElementoLeyenda(texto = "Sin plaga", color = MapaColores.Ninguna)
        ElementoLeyenda(texto = "Sin dato", color = MapaColores.SinDatos)
    }
}

@Composable
private fun ElementoLeyenda(texto: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
