package com.yldefonso.ojodchakra.screens.mapa

import com.yldefonso.ojodchakra.ui.iconos.IconosOjoD
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.LatLng
import com.yldefonso.ojodchakra.ui.componentes.EncabezadoPantalla
import com.yldefonso.ojodchakra.ui.componentes.Tarjeta
import com.yldefonso.ojodchakra.ui.mapa.AvisoSinConexion
import com.yldefonso.ojodchakra.ui.mapa.CENTRO_PREDETERMINADO
import com.yldefonso.ojodchakra.ui.mapa.LeyendaSeveridad
import com.yldefonso.ojodchakra.ui.mapa.MapaBase
import com.yldefonso.ojodchakra.ui.mapa.TarjetaParcelaMapa
import com.yldefonso.ojodchakra.ui.mapa.marcadoresDe
import com.yldefonso.ojodchakra.util.UbicacionHelper

/**
 * Mapa de parcelas. Solo funciona con internet: sin conexión muestra un aviso
 * y las coordenadas siguen disponibles en "Mis parcelas".
 */
@Composable
fun MapaParcelasScreen(
    onBack: () -> Unit,
    viewModel: MapaParcelasViewModel = viewModel()
) {
    val context = LocalContext.current
    val estado = remember { UbicacionHelper.estado(context) }
    val parcelas by viewModel.parcelas.collectAsState()

    var seleccionadaId by remember { mutableStateOf<String?>(null) }
    var enfocar by remember { mutableStateOf<LatLng?>(null) }

    val marcadores = marcadoresDe(parcelas)
    val seleccionada = parcelas.firstOrNull { it.id == seleccionadaId && it.tieneCoordenadas }
    val centro: LatLng = marcadores.firstOrNull()?.posicion ?: CENTRO_PREDETERMINADO
    val sinUbicacion = parcelas.count { !it.tieneCoordenadas }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EncabezadoPantalla(
            titulo = "Mapa de parcelas",
            subtitulo = "${marcadores.size} con ubicación",
            onVolver = onBack
        )

        if (!estado.hayInternet) {
            AvisoSinConexion(
                onVolver = onBack,
                modifier = Modifier.weight(1f)
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Acceso directo: "Todas" encuadra todas las parcelas; cada parcela centra su punto
                if (marcadores.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = enfocar == null,
                                onClick = {
                                    seleccionadaId = null
                                    enfocar = null
                                },
                                label = { Text("Todas") }
                            )
                        }
                        items(parcelas.filter { it.tieneCoordenadas }, key = { it.id }) { parcela ->
                            FilterChip(
                                selected = seleccionadaId == parcela.id,
                                onClick = {
                                    seleccionadaId = parcela.id
                                    enfocar = marcadores.firstOrNull { it.id == parcela.id }?.posicion
                                },
                                label = { Text(parcela.nombre) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = IconosOjoD.Parcela,
                                        contentDescription = null,
                                        modifier = androidx.compose.ui.Modifier.height(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                ) {
                    MapaBase(
                        centro = centro,
                        zoom = 13f,
                        marcadores = marcadores,
                        modifier = Modifier.fillMaxSize(),
                        enfocar = enfocar,
                        onMapaTocado = {
                            seleccionadaId = null
                            enfocar = null
                        },
                        onMarcadorTocado = { id -> seleccionadaId = id }
                    )

                    seleccionada?.let { parcela ->
                        TarjetaParcelaMapa(
                            parcela = parcela,
                            onCerrar = {
                                seleccionadaId = null
                                enfocar = null
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                        )
                    }
                }

                Tarjeta(padding = 12.dp) {
                    LeyendaSeveridad()
                    if (sinUbicacion > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$sinUbicacion parcela(s) sin ubicación no aparecen en el mapa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
