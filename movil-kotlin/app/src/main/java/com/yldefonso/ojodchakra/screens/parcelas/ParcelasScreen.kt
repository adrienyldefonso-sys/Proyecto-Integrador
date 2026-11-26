package com.yldefonso.ojodchakra.screens.parcelas

import com.yldefonso.ojodchakra.ui.iconos.IconosOjoD
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yldefonso.ojodchakra.data.model.ParcelaConConteo
import com.yldefonso.ojodchakra.ui.componentes.BotonPrincipal
import com.yldefonso.ojodchakra.ui.componentes.BotonSecundario
import com.yldefonso.ojodchakra.ui.componentes.EncabezadoPantalla
import com.yldefonso.ojodchakra.ui.componentes.EstadoVacio
import com.yldefonso.ojodchakra.ui.componentes.IconoContenedor
import com.yldefonso.ojodchakra.ui.componentes.Insignia
import com.yldefonso.ojodchakra.ui.componentes.Tarjeta
import com.yldefonso.ojodchakra.ui.componentes.TipoEstado
import com.yldefonso.ojodchakra.util.FormatoParcela
import com.yldefonso.ojodchakra.util.UbicacionHelper

@Composable
fun ParcelasScreen(
    onBack: () -> Unit,
    onRegistrar: () -> Unit,
    onParcelaClick: ((String) -> Unit)? = null,
    onSinParcela: (() -> Unit)? = null,
    onVerMapa: (() -> Unit)? = null,
    titulo: String = "Mis parcelas",
    viewModel: ParcelasViewModel = viewModel()
) {
    val parcelas by viewModel.parcelas.collectAsState()
    val lista = parcelas
    // El mapa solo se ofrece con internet
    val hayInternet = onVerMapa != null && UbicacionHelper.hayInternet(LocalContext.current)

    val subtitulo = when {
        lista == null -> "Cargando..."
        onParcelaClick != null -> "Elige dónde vas a escanear"
        lista.size == 1 -> "1 parcela registrada"
        else -> "${lista.size} parcelas registradas"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EncabezadoPantalla(
            titulo = titulo,
            subtitulo = subtitulo,
            onVolver = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            if (hayInternet) {
                BotonSecundario(
                    texto = "Ver parcelas en el mapa",
                    onClick = { onVerMapa?.invoke() },
                    icono = IconosOjoD.Mapa
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    lista == null -> CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    lista.isEmpty() -> EstadoVacio(
                        icono = IconosOjoD.Parcela,
                        titulo = "Aún no tienes parcelas",
                        descripcion = "Registra tu primera parcela para asociar cada diagnóstico al lugar correcto.",
                        modifier = Modifier.align(Alignment.Center)
                    )

                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(lista, key = { it.parcela.id }) { item ->
                            ParcelaItem(
                                item = item,
                                onClick = onParcelaClick?.let { clic -> { clic(item.parcela.id) } }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Solo aparece en modo selección
            if (onSinParcela != null) {
                BotonSecundario(
                    texto = "Escanear sin parcela",
                    onClick = onSinParcela
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            BotonPrincipal(
                texto = "Registrar parcela",
                onClick = onRegistrar,
                icono = Icons.Default.Add
            )
        }
    }
}

@Composable
private fun ParcelaItem(item: ParcelaConConteo, onClick: (() -> Unit)?) {
    val parcela = item.parcela
    val tieneDiagnosticos = item.totalDiagnosticos > 0

    Tarjeta(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconoContenedor(icono = IconosOjoD.Parcela, tamano = 46.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = parcela.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (parcela.referencia.isBlank()) parcela.cultivo
                    else "${parcela.cultivo} · ${parcela.referencia}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = FormatoParcela.coordenadas(parcela.latitud, parcela.longitud),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Insignia(
                texto = FormatoParcela.conteoDiagnosticos(item.totalDiagnosticos),
                tipo = if (tieneDiagnosticos) TipoEstado.EXITO else TipoEstado.NEUTRO
            )
        }
    }
}
