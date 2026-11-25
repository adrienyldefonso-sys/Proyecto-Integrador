package com.yldefonso.ojodchakra.screens.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yldefonso.ojodchakra.R
import com.yldefonso.ojodchakra.screens.sincronizacion.SincronizacionViewModel
import com.yldefonso.ojodchakra.screens.sincronizacion.TarjetaSincronizacion
import java.util.Calendar

private val VerdePrincipal = Color(0xFF2E6A38)
private val FondoPantalla = Color(0xFFFAFCFA)
private val VerdeSubtil = Color(0xFFEBF3EC)
private val TextoOscuro = Color(0xFF1B2E21)
private val TextoMuted = Color(0xFF5A7060)
private val Terracota = Color(0xFFA65A3A)
private val AmbarTexto = Color(0xFF8D6E00)
private val AmbarFondo = Color(0xFFFFF8E1)

private fun saludoSegunLaHora(): String {
    val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hora) {
        in 5..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

@Composable
fun HomeScreen(
    onStartScan: () -> Unit,
    onVerHistorial: () -> Unit,
    onVerParcelas: () -> Unit,
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
            .background(FondoPantalla)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Encabezado
            Row(verticalAlignment = Alignment.CenterVertically) {
                LogoConOnda()
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = saludo, fontSize = 14.sp, color = TextoMuted)
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = VerdePrincipal)) { append("OJO") }
                            withStyle(SpanStyle(color = Terracota)) { append("D") }
                            withStyle(SpanStyle(color = VerdePrincipal)) { append("CHAKRA") }
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "Diagnóstico agrícola con inteligencia artificial",
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        color = TextoMuted
                    )
                }
            }

            // Estado
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(
                    icono = Icons.Default.CheckCircle,
                    texto = "IA offline lista",
                    tinte = VerdePrincipal,
                    fondo = VerdeSubtil
                )
                if (pendientes == 0) {
                    Chip(
                        icono = Icons.Default.CloudDone,
                        texto = "Todo sincronizado",
                        tinte = VerdePrincipal,
                        fondo = VerdeSubtil
                    )
                } else {
                    Chip(
                        icono = Icons.Default.CloudOff,
                        texto = "$pendientes por enviar",
                        tinte = AmbarTexto,
                        fondo = AmbarFondo
                    )
                }
            }

            // Tu campo
            Etiqueta("TU CAMPO")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaResumen(
                    icono = Icons.Default.LocationOn,
                    valor = totalParcelas,
                    etiqueta = if (totalParcelas == 1) "Parcela" else "Parcelas",
                    descripcion = "Gestionar áreas de cultivo",
                    onClick = onVerParcelas,
                    modifier = Modifier.weight(1f)
                )
                TarjetaResumen(
                    icono = Icons.Default.History,
                    valor = totalReportes,
                    etiqueta = if (totalReportes == 1) "Reporte" else "Reportes",
                    descripcion = "Consultar el historial",
                    onClick = onVerHistorial,
                    modifier = Modifier.weight(1f)
                )
            }

            // Conexión
            Etiqueta("CONEXIÓN")
            TarjetaSincronizacion(
                pendientes = pendientes,
                estado = estadoSincronizacion,
                onSincronizar = sincronizacionViewModel::sincronizar
            )

            // Consejos
            Etiqueta("BUENAS PRÁCTICAS")
            ConsejosFoto()

            Text(
                text = "OjoDChakra v1.0 · Proyecto Integrador · TECSUP",
                fontSize = 12.sp,
                color = TextoMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 16.dp)
            )
        }

        BarraEscanear(onClick = onStartScan)
    }
}

@Composable
private fun LogoConOnda() {
    val transicion = rememberInfiniteTransition(label = "onda")
    val pulso by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulso"
    )

    Box(
        modifier = Modifier
            .size(96.dp)
            .drawBehind {
                val radioMaximo = size.minDimension / 2f
                drawCircle(color = VerdePrincipal.copy(alpha = 0.06f), radius = radioMaximo)
                drawCircle(color = VerdePrincipal.copy(alpha = 0.10f), radius = radioMaximo * 0.82f)
                drawCircle(
                    color = VerdePrincipal.copy(alpha = 0.22f * (1f - pulso)),
                    radius = radioMaximo * (0.60f + 0.40f * pulso),
                    style = Stroke(width = 2.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_ojodchakra),
            contentDescription = "Logotipo de OjoDChakra",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Text(
        text = texto,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = TextoMuted
    )
}

@Composable
private fun Chip(icono: ImageVector, texto: String, tinte: Color, fondo: Color) {
    Surface(shape = RoundedCornerShape(50), color = fondo) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = tinte,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = texto,
                color = tinte,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TarjetaResumen(
    icono: ImageVector,
    valor: Int?,
    etiqueta: String,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, VerdePrincipal.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(VerdeSubtil),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = VerdePrincipal,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = valor?.toString() ?: "–",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextoOscuro
            )
            Text(
                text = etiqueta,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
            Text(text = descripcion, fontSize = 12.sp, color = TextoMuted)
        }
    }
}

@Composable
private fun ConsejosFoto() {
    Surface(shape = RoundedCornerShape(20.dp), color = VerdeSubtil) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Para una foto confiable",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
            ConsejoLinea("1", "Acércate: que la hoja llene casi todo el cuadro.")
            ConsejoLinea("2", "Usa luz natural o pareja, sin sombras fuertes.")
            ConsejoLinea("3", "Una sola hoja a la vez, sin otros objetos alrededor.")
        }
    }
}

@Composable
private fun ConsejoLinea(numero: String, texto: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(VerdePrincipal),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = numero,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = texto, fontSize = 14.sp, color = TextoOscuro)
    }
}

@Composable
private fun BarraEscanear(onClick: () -> Unit) {
    Surface(color = Color.White, shadowElevation = 12.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdePrincipal,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Escanear hoja de cultivo",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Funciona sin internet",
                fontSize = 12.sp,
                color = TextoMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            )
        }
    }
}