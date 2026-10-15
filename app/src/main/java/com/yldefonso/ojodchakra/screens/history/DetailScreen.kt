package com.yldefonso.ojodchakra.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

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
private val FondoPendiente = Color(0xFFFFF3E0)
private val TextoPendiente = Color(0xFFD84315)
private val FondoSincronizado = Color(0xFFE8F5E9)
private val TextoSincronizado = Color(0xFF2E6A38)

@Composable
fun DetailScreen(
    id: String,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val diagnostico by remember(id) { viewModel.observarDiagnostico(id) }
        .collectAsState(initial = null)
    val d = diagnostico

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        if (d == null) {
            // ESTADO REPORTE NO ENCONTRADO
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TextoMuted,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No se encontró el reporte solicitado",
                    style = MaterialTheme.typography.titleMedium.copy(color = TextoOscuro)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)
                ) {
                    Text("Volver al historial")
                }
            }
        } else {
            val esPlantaSana = d.plagaEnEspanol.contains("sana", ignoreCase = true)
            val porcentajeCalculado = if (d.confianza <= 1.0f) d.confianza * 100f else d.confianza
            val progresoBarra = (porcentajeCalculado / 100f).coerceIn(0f, 1f)
            val esPendiente = d.estadoSincronizacion == "PENDIENTE"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ==========================================
                // 1. ENCABEZADO CON BOTÓN DE REGRESO
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(VerdeSubtil)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = VerdePrincipal
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Detalle del reporte",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TextoOscuro,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ==========================================
                // 2. FOTO DE MUESTRA AMPLIADA
                // ==========================================
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color.Black.copy(alpha = 0.12f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = SuperficieTarjeta,
                    border = BorderStroke(1.5.dp, VerdePrincipal.copy(alpha = 0.2f))
                ) {
                    FotoReporte(
                        ruta = d.rutaFoto,
                        ladoMaximo = 1024,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // 3. TARJETA PRINCIPAL DE DIAGNÓSTICO
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
                        // Badge de Diagnóstico (Saludable vs Anomalía)
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
                            text = d.plagaEnEspanol,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = TextoOscuro,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Fecha y hora de captura (Ajustada a hora de Perú)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = TextoMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Capturado el " + formatearFecha(d.fechaCaptura),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextoMuted,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nivel de confianza y barra de precisión
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

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chip de estado de sincronización
                        Surface(
                            color = if (esPendiente) FondoPendiente else FondoSincronizado,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (esPendiente) Icons.Default.Sync else Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = if (esPendiente) TextoPendiente else TextoSincronizado,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = textoEstado(d.estadoSincronizacion),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (esPendiente) TextoPendiente else TextoSincronizado,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // 4. BLOQUES DE SEVERIDAD Y RECOMENDACIÓN
                // ==========================================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nivel de severidad
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SuperficieTarjeta,
                        border = BorderStroke(1.dp, VerdePrincipal.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = VerdeSubtil
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.HealthAndSafety,
                                        contentDescription = null,
                                        tint = VerdePrincipal,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Nivel de severidad",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = TextoMuted,
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = d.nivelSeveridad ?: "Sin evaluar",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextoOscuro,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                )
                            }
                        }
                    }

                    // Recomendación de tratamiento
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SuperficieTarjeta,
                        border = BorderStroke(1.dp, VerdePrincipal.copy(alpha = 0.12f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = VerdeSubtil
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.MedicalServices,
                                            contentDescription = null,
                                            tint = VerdePrincipal,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "Recomendación de tratamiento",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = TextoMuted,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = d.recomendacion ?: "Aún no disponible",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextoOscuro,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ==========================================
                // 5. BOTÓN REGRESAR AL HISTORIAL
                // ==========================================
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), spotColor = VerdePrincipal),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdePrincipal,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Volver al historial",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}