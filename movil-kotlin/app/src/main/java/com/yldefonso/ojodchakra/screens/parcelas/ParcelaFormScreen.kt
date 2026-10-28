package com.yldefonso.ojodchakra.screens.parcelas

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yldefonso.ojodchakra.util.UbicacionHelper

private val VerdePrincipal = Color(0xFF2E6A38)
private val FondoPantalla = Color(0xFFFAFCFA)
private val VerdeSubtil = Color(0xFFEBF3EC)
private val TextoOscuro = Color(0xFF1B2E21)
private val TextoMuted = Color(0xFF5A7060)
private val TextoAlerta = Color(0xFFE65100)

@Composable
fun ParcelaFormScreen(
    onBack: () -> Unit,
    onGuardada: () -> Unit,
    viewModel: ParcelaFormViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current

    val buscarUbicacion = {
        viewModel.onBuscandoUbicacion()
        UbicacionHelper.obtener(context) { ubicacion ->
            viewModel.onUbicacionObtenida(ubicacion?.latitude, ubicacion?.longitude)
        }
    }

    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos.values.any { it }) {
            buscarUbicacion()
        } else {
            viewModel.mostrarMensaje("Sin permiso de ubicación. Puedes escribir las coordenadas a mano.")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
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
                    text = "Registrar parcela",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextoOscuro,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = "Se guarda en tu celular",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextoMuted)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Campo(
                valor = estado.nombre,
                etiqueta = "Nombre de la parcela",
                error = estado.errores.nombre,
                onCambio = viewModel::onNombreChange
            )
            Campo(
                valor = estado.cultivo,
                etiqueta = "Cultivo",
                error = estado.errores.cultivo,
                onCambio = viewModel::onCultivoChange
            )
            Campo(
                valor = estado.referencia,
                etiqueta = "Referencia del lugar (opcional)",
                error = null,
                onCambio = viewModel::onReferenciaChange
            )

            Text(
                text = "Ubicación",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = VerdePrincipal,
                    fontWeight = FontWeight.Bold
                )
            )

            OutlinedButton(
                onClick = {
                    val tienePermiso =
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED

                    if (tienePermiso) {
                        buscarUbicacion()
                    } else {
                        permisoLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                enabled = !estado.buscandoUbicacion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdePrincipal)
            ) {
                Text(
                    text = if (estado.buscandoUbicacion) "Buscando ubicación..." else "Usar mi ubicación",
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Campo(
                    valor = estado.latitud,
                    etiqueta = "Latitud",
                    error = estado.errores.latitud,
                    onCambio = viewModel::onLatitudChange,
                    modifier = Modifier.weight(1f)
                )
                Campo(
                    valor = estado.longitud,
                    etiqueta = "Longitud",
                    error = estado.errores.longitud,
                    onCambio = viewModel::onLongitudChange,
                    modifier = Modifier.weight(1f)
                )
            }

            estado.mensaje?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextoAlerta,
                        fontSize = 13.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.guardar(onGuardada) },
            enabled = !estado.guardando,
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
                text = if (estado.guardando) "Guardando..." else "Guardar parcela",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun Campo(
    valor: String,
    etiqueta: String,
    error: String?,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdePrincipal,
            focusedLabelColor = VerdePrincipal,
            cursorColor = VerdePrincipal
        )
    )
}