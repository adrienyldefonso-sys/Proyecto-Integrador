package com.yldefonso.ojodchakra.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet4Bar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yldefonso.ojodchakra.R

// Paleta de Colores Limpia y Orgánica (AgriTech)
private val VerdePrincipal = Color(0xFF2E6A38)
private val CafeTierra = Color(0xFF795548)
private val FondoPantalla = Color(0xFFFAFCFA)
private val SuperficieTarjeta = Color(0xFFFFFFFF)
private val VerdeSubtil = Color(0xFFEBF3EC)
private val TextoOscuro = Color(0xFF1B2E21)
private val TextoMuted = Color(0xFF5A7060)

@Composable
fun HomeScreen(
    onStartScan: () -> Unit,
    onVerHistorial: () -> Unit,
    onVerParcelas: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {

        // ==========================================
        // BLOQUE CENTRAL AGRUPADO (SIN ESPACIOS VACÍOS)
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. LOGO CENTRADO Y AGRANDADO
            Image(
                painter = painterResource(id = R.drawable.logo_ojodchakra),
                contentDescription = "Logo OjoDChakra",
                modifier = Modifier
                    .size(190.dp)
                    .padding(bottom = 8.dp)
            )

            // 2. TÍTULO DE MARCA
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = VerdePrincipal, fontWeight = FontWeight.ExtraBold)) {
                        append("OJO")
                    }
                    withStyle(style = SpanStyle(color = CafeTierra, fontWeight = FontWeight.ExtraBold)) {
                        append("D")
                    }
                    withStyle(style = SpanStyle(color = VerdePrincipal, fontWeight = FontWeight.ExtraBold)) {
                        append("CHAKRA")
                    }
                },
                fontSize = 32.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 3. SUBTÍTULO RESALTANTE
            Text(
                text = "Diagnóstico Agrícola con Inteligencia Artificial",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextoOscuro,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. PÍLDORA DE ESTADO INTEGRADA
            Surface(
                shape = RoundedCornerShape(50),
                color = VerdeSubtil,
                border = androidx.compose.foundation.BorderStroke(1.dp, VerdePrincipal.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = VerdePrincipal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "IA Offline Lista",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = VerdePrincipal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .width(1.dp)
                            .background(VerdePrincipal.copy(alpha = 0.3f))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SignalCellularConnectedNoInternet4Bar,
                            contentDescription = null,
                            tint = TextoMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sin internet",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextoMuted,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. BOTONES DE ACCIÓN COMPACTOS Y ELEGANTES
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // --- BOTÓN PRINCIPAL: ESCANEAR CULTIVO ---
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = VerdePrincipal.copy(alpha = 0.35f)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onStartScan() },
                    shape = RoundedCornerShape(20.dp),
                    color = VerdePrincipal
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Escanear cultivo",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                )
                                Text(
                                    text = "Analizar hoja con la cámara",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // --- BOTÓN SECUNDARIO: MIS PARCELAS ---
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onVerParcelas() },
                    shape = RoundedCornerShape(20.dp),
                    color = SuperficieTarjeta,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, VerdePrincipal.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(VerdeSubtil),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = VerdePrincipal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Mis parcelas",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextoOscuro,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = "Gestionar áreas de cultivo",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextoMuted,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = VerdePrincipal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // --- BOTÓN TERCIARIO: VER HISTORIAL ---
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onVerHistorial() },
                    shape = RoundedCornerShape(20.dp),
                    color = SuperficieTarjeta,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, VerdePrincipal.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(VerdeSubtil),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = VerdePrincipal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Ver historial",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextoOscuro,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = "Consultar reportes guardados",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextoMuted,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = VerdePrincipal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // PIE DE PÁGINA ANCLADO AL FONDO
        // ==========================================
        Text(
            text = "OjoDChakra v1.0 • Sprint 1 MVP",
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextoMuted.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}