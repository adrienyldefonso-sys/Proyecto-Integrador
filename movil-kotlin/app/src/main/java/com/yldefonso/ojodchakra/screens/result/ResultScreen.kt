package com.yldefonso.ojodchakra.screens.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yldefonso.ojodchakra.screens.camera.CameraViewModel

// Paleta de Colores Institucional OjoDChakra
private val VerdePrincipal = Color(0xFF2E6A38)
private val FondoPantalla = Color(0xFFFAFCFA)
private val SuperficieTarjeta = Color(0xFFFFFFFF)
private val VerdeSubtil = Color(0xFFEBF3EC)
private val TextoOscuro = Color(0xFF1B2E21)
private val TextoMuted = Color(0xFF5A7060)

// Estados de alerta y diagnóstico
private val FondoSano = Color(0xFFE8F5E9)
private val TextoSano = Color(0xFF2E7D32)
private val FondoAlerta = Color(0xFFFFF3E0)
private val TextoAlerta = Color(0xFFE65100)

@Composable
fun ResultScreen(
    viewModel: CameraViewModel,
    onScanAgain: () -> Unit,
    onGoHome: () -> Unit
) {
    val bitmap = viewModel.capturedBitmap.value
    val result = viewModel.result.value

    val porcentajeCalculado = result?.let {
        if (it.confidence <= 1.0f) it.confidence * 100f else it.confidence
    } ?: 0f
    val progresoBarra = (porcentajeCalculado / 100f).coerceIn(0f, 1f)
    val esPlantaSana = result?.labelEnEspanol?.contains("sana", ignoreCase = true) == true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ==========================================
            // 1. BARRA SUPERIOR CON NAVEGACIÓN Y TÍTULO
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón superior para volver al Inicio
                IconButton(
                    onClick = {
                        viewModel.reset()
                        onGoHome()
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(VerdeSubtil)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Volver al inicio",
                        tint = VerdePrincipal
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Resultado del Diagnóstico",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TextoOscuro,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = "Procesado con TFLite local",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextoMuted
                        )
                    )
                }

                // Espaciador invisible para mantener equilibrado el título
                Spacer(modifier = Modifier.size(42.dp))
            }

            // ==========================================
            // 2. FOTO DE LA MUESTRA CAPTURADA
            // ==========================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color.Black.copy(alpha = 0.12f)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = SuperficieTarjeta,
                border = BorderStroke(1.5.dp, VerdePrincipal.copy(alpha = 0.2f))
            ) {
                Box {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Foto capturada",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(VerdeSubtil),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sin imagen disponible", color = TextoMuted)
                        }
                    }

                    // Etiqueta flotante
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Muestra analizada",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 3. TARJETA DE DIAGNÓSTICO
            // ==========================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = VerdePrincipal.copy(alpha = 0.15f)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = SuperficieTarjeta,
                border = BorderStroke(1.dp, VerdePrincipal.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (result != null) {
                        // Badge de tipo de hallazgo (Texto limpio sin ícono/emoji de check)
                        Surface(
                            color = if (esPlantaSana) FondoSano else FondoAlerta,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (esPlantaSana) "CULTIVO SALUDABLE" else "ANOMALÍA DETECTADA",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (esPlantaSana) TextoSano else TextoAlerta,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Diagnóstico principal
                        Text(
                            text = result.labelEnEspanol,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = TextoOscuro,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Porcentaje y Barra de Nivel de Confianza
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Nivel de confianza",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextoMuted,
                                        fontSize = 13.sp
                                    )
                                )
                                Text(
                                    text = String.format("%.2f%%", porcentajeCalculado),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = VerdePrincipal,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { progresoBarra },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = if (esPlantaSana) VerdePrincipal else TextoAlerta,
                                trackColor = VerdeSubtil
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        HorizontalDivider(color = VerdeSubtil, thickness = 1.dp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Guardado automáticamente en el historial local",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextoMuted,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    } else {
                        Text(
                            text = "No se pudo obtener un diagnóstico",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = TextoOscuro,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 4. BOTONES DE ACCIÓN (ESCANEAR & INICIO)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Botón Primario: Escanear otra vez
                Button(
                    onClick = {
                        viewModel.reset()
                        onScanAgain()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), spotColor = VerdePrincipal),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdePrincipal,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Escanear otra vez",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Botón Secundario: Volver al inicio
                OutlinedButton(
                    onClick = {
                        viewModel.reset()
                        onGoHome()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, VerdePrincipal.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = VerdePrincipal
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Volver al inicio",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}