package com.yldefonso.ojodchakra.screens.sincronizacion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yldefonso.ojodchakra.sincronizacion.textoPendientes

private val VerdePrincipal = Color(0xFF2E6A38)
private val TextoOscuro = Color(0xFF1B2E21)
private val TextoMuted = Color(0xFF5A7060)

@Composable
fun TarjetaSincronizacion(
    modifier: Modifier = Modifier,
    viewModel: SincronizacionViewModel = viewModel()
) {
    val pendientes by viewModel.pendientes.collectAsState()
    val estado by viewModel.estado.collectAsState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, VerdePrincipal.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Sincronización con el servidor",
                color = TextoOscuro,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(text = textoPendientes(pendientes), color = TextoMuted, fontSize = 14.sp)
            estado.mensaje?.let {
                Text(text = it, color = TextoMuted, fontSize = 13.sp)
            }
            Button(
                onClick = viewModel::sincronizar,
                enabled = !estado.sincronizando && pendientes > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdePrincipal,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (estado.sincronizando) "Sincronizando..." else "Sincronizar ahora",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}