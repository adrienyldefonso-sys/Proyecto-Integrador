package com.yldefonso.ojodchakra.screens.sincronizacion

import com.yldefonso.ojodchakra.ui.iconos.IconosOjoD
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yldefonso.ojodchakra.sincronizacion.textoPendientes
import com.yldefonso.ojodchakra.ui.componentes.BotonPrincipal
import com.yldefonso.ojodchakra.ui.componentes.Insignia
import com.yldefonso.ojodchakra.ui.componentes.Tarjeta
import com.yldefonso.ojodchakra.ui.componentes.TipoEstado

/** Envío al servidor: estado de los registros pendientes y botón para enviarlos. */
@Composable
fun TarjetaSincronizacion(
    pendientes: Int,
    estado: EstadoSincronizacion,
    onSincronizar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alDia = pendientes == 0

    Tarjeta(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                Text(
                    text = "Envío al servidor",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = textoPendientes(pendientes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Insignia(
                texto = if (alDia) "Al día" else "Pendiente",
                tipo = if (alDia) TipoEstado.EXITO else TipoEstado.ALERTA,
                icono = if (alDia) IconosOjoD.NubeOk else IconosOjoD.NubeOff
            )
        }

        estado.mensaje?.let {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        BotonPrincipal(
            texto = when {
                estado.sincronizando -> "Sincronizando..."
                alDia -> "Buscar revisiones"
                else -> "Sincronizar ahora"
            },
            onClick = onSincronizar,
            habilitado = !estado.sincronizando
        )
    }
}
