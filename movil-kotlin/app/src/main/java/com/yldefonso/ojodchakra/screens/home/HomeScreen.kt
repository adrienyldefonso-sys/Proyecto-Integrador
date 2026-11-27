package com.yldefonso.ojodchakra.screens.home

import com.yldefonso.ojodchakra.sesion.SesionUsuario
import com.yldefonso.ojodchakra.ui.componentes.AvatarIniciales
import com.yldefonso.ojodchakra.ui.componentes.inicialesDe

import com.yldefonso.ojodchakra.ui.iconos.IconosOjoD
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yldefonso.ojodchakra.R
import com.yldefonso.ojodchakra.screens.sincronizacion.SincronizacionViewModel
import com.yldefonso.ojodchakra.screens.sincronizacion.TarjetaSincronizacion
import com.yldefonso.ojodchakra.ui.componentes.BotonPrincipal
import com.yldefonso.ojodchakra.ui.componentes.EtiquetaSeccion
import com.yldefonso.ojodchakra.ui.componentes.FondoCurvasNivel
import com.yldefonso.ojodchakra.ui.componentes.Insignia
import com.yldefonso.ojodchakra.ui.componentes.Tarjeta
import com.yldefonso.ojodchakra.ui.componentes.TipoEstado
import com.yldefonso.ojodchakra.ui.theme.LocalColoresApp
import java.util.Calendar

private fun saludoSegunLaHora(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

private val consejosFoto = listOf(
    "Acércate para que la hoja llene casi todo el cuadro.",
    "Usa luz natural o pareja, sin sombras fuertes.",
    "Muestra una sola hoja, sin otros objetos alrededor."
)

@Composable
fun HomeScreen(
    onStartScan: () -> Unit,
    onVerHistorial: () -> Unit,
    onVerParcelas: () -> Unit,
    onAjustes: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel(),
    sincronizacionViewModel: SincronizacionViewModel = viewModel()
) {
    val totalParcelas by homeViewModel.totalParcelas.collectAsState()
    val totalReportes by homeViewModel.totalReportes.collectAsState()
    val pendientes by sincronizacionViewModel.pendientes.collectAsState()
    val estadoSincronizacion by sincronizacionViewModel.estado.collectAsState()
    val saludo = remember { saludoSegunLaHora() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            CabeceraInicio(saludo = saludo, onAjustes = onAjustes)

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                PulsoEquipo(totalReportes = totalReportes, pendientes = pendientes)

                Column {
                    EtiquetaSeccion("Tu campo")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TileParcelas(
                            valor = totalParcelas,
                            onClick = onVerParcelas,
                            modifier = Modifier.weight(1f)
                        )
                        TileReportes(
                            valor = totalReportes,
                            onClick = onVerHistorial,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Column {
                    EtiquetaSeccion("Conexión")
                    Spacer(modifier = Modifier.height(12.dp))
                    TarjetaSincronizacion(
                        pendientes = pendientes,
                        estado = estadoSincronizacion,
                        onSincronizar = sincronizacionViewModel::sincronizar
                    )
                }

                Column {
                    EtiquetaSeccion("Para una foto confiable")
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 4.dp)
                    ) {
                        itemsIndexed(consejosFoto) { indice, texto ->
                            ConsejoTarjeta(numero = "%02d".format(indice + 1), texto = texto)
                        }
                    }
                }

                Text(
                    text = "OjoDChakra v1.0 · Proyecto Integrador · TECSUP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 8.dp)
                )
            }
        }

        BarraEscanear(onClick = onStartScan)
    }
}

@Composable
private fun CabeceraInicio(saludo: String, onAjustes: () -> Unit) {
    val colores = LocalColoresApp.current
    Surface(
        color = colores.bandaFondo,
        contentColor = colores.bandaTexto,
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            FondoCurvasNivel(
                modifier = Modifier.matchParentSize(),
                color = colores.bandaTexto.copy(alpha = 0.08f)
            )
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 26.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (SesionUsuario.cuentaActiva) {
                    AvatarIniciales(
                        texto = inicialesDe(SesionUsuario.nombre ?: SesionUsuario.usuario),
                        tamano = 58.dp
                    )
                } else Image(
                    painter = painterResource(id = R.drawable.logo_ojodchakra),
                    contentDescription = "Logotipo de OjoDChakra",
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (SesionUsuario.cuentaActiva) "BIENVENIDO" else saludo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colores.bandaTexto.copy(alpha = 0.8f)
                    )
                    Text(
                        text = if (SesionUsuario.cuentaActiva) SesionUsuario.nombre.orEmpty() else "OjoDChakra",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colores.bandaTexto
                    )
                    Text(
                        text = "Diagnóstico de plagas en papa",
                        style = MaterialTheme.typography.bodySmall,
                        color = colores.bandaTexto.copy(alpha = 0.8f)
                    )
                }
                IconButton(
                    onClick = onAjustes,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colores.bandaTexto.copy(alpha = 0.14f))
                ) {
                    Icon(
                        imageVector = IconosOjoD.Ajustes,
                        contentDescription = "Ajustes",
                        tint = colores.bandaTexto
                    )
                }
            }
        }
    }
}

/** Barra del equipo: qué parte de los reportes ya se envió y qué parte falta. */
@Composable
private fun PulsoEquipo(totalReportes: Int?, pendientes: Int) {
    val colores = LocalColoresApp.current
    val total = totalReportes ?: 0
    val pendientesAcotados = pendientes.coerceIn(0, maxOf(total, pendientes))
    val enviados = (total - pendientesAcotados).coerceAtLeast(0)
    val fraccionEnviada = if (total == 0) 1f else enviados.toFloat() / total

    Tarjeta(padding = 18.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Estado del equipo",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "El análisis funciona aunque no haya señal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Insignia(
                texto = "Activo",
                tipo = TipoEstado.EXITO,
                icono = IconosOjoD.Verificado
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(colores.alertaFondo)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraccionEnviada.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "$enviados enviados",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (pendientes == 0) "Todo al día" else "$pendientes por enviar",
                style = MaterialTheme.typography.labelMedium,
                color = if (pendientes == 0) MaterialTheme.colorScheme.onSurfaceVariant else colores.alertaTexto
            )
        }
    }
}

/** Parcelas: tarjeta en verde de marca para dar contraste. */
@Composable
private fun TileParcelas(valor: Int?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val contenido = MaterialTheme.colorScheme.onPrimary
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = contenido
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (valor == 1) "Parcela" else "Parcelas",
                    style = MaterialTheme.typography.labelLarge,
                    color = contenido.copy(alpha = 0.85f)
                )
                Icon(
                    imageVector = IconosOjoD.Parcela,
                    contentDescription = null,
                    tint = contenido,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = valor?.toString() ?: "–",
                style = MaterialTheme.typography.displaySmall,
                color = contenido
            )
            Text(
                text = "Áreas de cultivo",
                style = MaterialTheme.typography.bodySmall,
                color = contenido.copy(alpha = 0.8f)
            )
        }
    }
}

/** Reportes: tarjeta clara con sombra suave. */
@Composable
private fun TileReportes(valor: Int?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (valor == 1) "Reporte" else "Reportes",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = IconosOjoD.Historial,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = valor?.toString() ?: "–",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Diagnósticos guardados",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConsejoTarjeta(numero: String, texto: String) {
    Surface(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = numero,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BarraEscanear(onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BotonPrincipal(
                texto = "Escanear hoja de cultivo",
                onClick = onClick,
                icono = IconosOjoD.Escanear
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Funciona sin internet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
