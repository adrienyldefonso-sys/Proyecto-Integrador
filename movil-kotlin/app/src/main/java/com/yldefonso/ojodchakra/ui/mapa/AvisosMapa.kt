package com.yldefonso.ojodchakra.ui.mapa

import com.yldefonso.ojodchakra.ui.iconos.IconosOjoD
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yldefonso.ojodchakra.data.model.ParcelaEnMapa
import com.yldefonso.ojodchakra.ui.componentes.BotonPrincipal
import com.yldefonso.ojodchakra.ui.componentes.BotonSecundario
import com.yldefonso.ojodchakra.ui.componentes.IconoContenedor
import com.yldefonso.ojodchakra.ui.componentes.Insignia
import com.yldefonso.ojodchakra.ui.componentes.Tarjeta
import com.yldefonso.ojodchakra.ui.componentes.TipoEstado
import com.yldefonso.ojodchakra.util.FormatoParcela
import com.yldefonso.ojodchakra.util.SeveridadMapa

/** Aviso que reemplaza al mapa cuando no hay internet. */
@Composable
fun AvisoSinConexion(onVolver: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconoContenedor(icono = IconosOjoD.NubeOff, tamano = 72.dp)
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "El mapa necesita internet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sin conexión, las coordenadas de cada parcela siguen disponibles en Mis parcelas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(22.dp))
        BotonSecundario(texto = "Volver", onClick = onVolver)
    }
}

/** Tarjeta con el resumen de la parcela tocada en el mapa. */
@Composable
fun TarjetaParcelaMapa(
    parcela: ParcelaEnMapa,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tipo = when (parcela.severidad?.uppercase()) {
        "ALTA" -> TipoEstado.PELIGRO
        "MEDIA" -> TipoEstado.ALERTA
        "NINGUNA" -> TipoEstado.EXITO
        else -> TipoEstado.INFO
    }
    Tarjeta(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoContenedor(icono = IconosOjoD.Parcela, tamano = 44.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = parcela.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = parcela.cultivo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Insignia(texto = SeveridadMapa.etiqueta(parcela.severidad), tipo = tipo)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = FormatoParcela.coordenadas(parcela.latitud, parcela.longitud),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        BotonPrincipal(texto = "Cerrar", onClick = onCerrar)
    }
}
