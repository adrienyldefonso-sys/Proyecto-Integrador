package com.yldefonso.ojodchakra.screens.parcelas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yldefonso.ojodchakra.data.model.ParcelaConConteo
import com.yldefonso.ojodchakra.util.FormatoParcela

private val VerdePrincipal = Color(0xFF2E6A38)
private val FondoPantalla = Color(0xFFFAFCFA)
private val SuperficieTarjeta = Color(0xFFFFFFFF)
private val VerdeSubtil = Color(0xFFEBF3EC)
private val TextoOscuro = Color(0xFF1B2E21)
private val TextoMuted = Color(0xFF5A7060)

@Composable
fun ParcelasScreen(
    onBack: () -> Unit,
    onRegistrar: () -> Unit,
    onParcelaClick: ((String) -> Unit)? = null,
    viewModel: ParcelasViewModel = viewModel()
) {
    val parcelas by viewModel.parcelas.collectAsState()
    val lista = parcelas

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Barra superior
        Row(
            modifier = Modifier.fillMaxWidth(),
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
            Column {
                Text(
                    text = "Mis parcelas",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextoOscuro,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = when {
                        lista == null -> "Cargando..."
                        lista.size == 1 -> "1 parcela registrada"
                        else -> "${lista.size} parcelas registradas"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(color = TextoMuted)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                lista == null -> CircularProgressIndicator(
                    color = VerdePrincipal,
                    modifier = Modifier.align(Alignment.Center)
                )

                lista.isEmpty() -> EstadoVacio(modifier = Modifier.align(Alignment.Center))

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

        Button(
            onClick = onRegistrar,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdePrincipal,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Registrar parcela",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ParcelaItem(item: ParcelaConConteo, onClick: (() -> Unit)?) {
    val parcela = item.parcela

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(18.dp),
        color = SuperficieTarjeta,
        border = BorderStroke(1.dp, VerdePrincipal.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = parcela.nombre,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextoOscuro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = VerdeSubtil,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = FormatoParcela.conteoDiagnosticos(item.totalDiagnosticos),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (item.totalDiagnosticos > 0) VerdePrincipal else TextoMuted,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (parcela.referencia.isBlank()) parcela.cultivo
                else "${parcela.cultivo} · ${parcela.referencia}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextoOscuro,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = FormatoParcela.coordenadas(parcela.latitud, parcela.longitud),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextoMuted,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
private fun EstadoVacio(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Aún no tienes parcelas",
            style = MaterialTheme.typography.titleMedium.copy(
                color = TextoOscuro,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Registra tu primera parcela para asociar cada diagnóstico al lugar correcto.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextoMuted,
                textAlign = TextAlign.Center
            )
        )
    }
}