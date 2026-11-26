package com.yldefonso.ojodchakra.screens.parcelas

import com.yldefonso.ojodchakra.ui.iconos.IconosOjoD
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yldefonso.ojodchakra.ui.componentes.BotonPrincipal
import com.yldefonso.ojodchakra.ui.componentes.BotonSecundario
import com.yldefonso.ojodchakra.ui.componentes.EncabezadoPantalla
import com.yldefonso.ojodchakra.ui.componentes.EtiquetaSeccion
import com.yldefonso.ojodchakra.ui.componentes.IconoContenedor
import com.yldefonso.ojodchakra.ui.componentes.Insignia
import com.yldefonso.ojodchakra.ui.componentes.Tarjeta
import com.yldefonso.ojodchakra.ui.componentes.TipoEstado
import com.yldefonso.ojodchakra.ui.mapa.SelectorUbicacionMapa
import com.yldefonso.ojodchakra.util.FalloUbicacion
import com.yldefonso.ojodchakra.util.UbicacionEncontrada
import com.yldefonso.ojodchakra.util.UbicacionHelper
import com.yldefonso.ojodchakra.util.ValidadorParcela

@Composable
fun ParcelaFormScreen(
    onBack: () -> Unit,
    onGuardada: () -> Unit,
    viewModel: ParcelaFormViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current
    var estadoUbicacion by remember { mutableStateOf(UbicacionHelper.estado(context)) }

    val buscarUbicacion = {
        viewModel.onBuscandoUbicacion()
        estadoUbicacion = UbicacionHelper.estado(context)
        UbicacionHelper.obtener(context) { resultado ->
            when (resultado) {
                is UbicacionEncontrada -> viewModel.onUbicacionObtenida(
                    resultado.latitud, resultado.longitud, resultado.precisionMetros,
                    resultado.esGps, resultado.antiguedadMin
                )
                is FalloUbicacion -> viewModel.mostrarMensaje(resultado.mensaje)
            }
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
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        EncabezadoPantalla(
            titulo = "Nueva parcela",
            subtitulo = "Se guarda en tu celular",
            onVolver = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EtiquetaSeccion("Datos del terreno")
            Tarjeta {
                Campo(
                    valor = estado.nombre,
                    etiqueta = "Nombre de la parcela",
                    error = estado.errores.nombre,
                    onCambio = viewModel::onNombreChange
                )
                Spacer(modifier = Modifier.height(12.dp))
                Campo(
                    valor = estado.cultivo,
                    etiqueta = "Cultivo",
                    error = estado.errores.cultivo,
                    onCambio = viewModel::onCultivoChange
                )
                Spacer(modifier = Modifier.height(12.dp))
                Campo(
                    valor = estado.referencia,
                    etiqueta = "Referencia del lugar (opcional)",
                    error = null,
                    onCambio = viewModel::onReferenciaChange
                )
            }

            EtiquetaSeccion("Ubicación (opcional)")
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconoContenedor(icono = IconosOjoD.Parcela, tamano = 42.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = estadoUbicacion.textoAyuda(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = estadoUbicacion.textoProveedores(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                BotonSecundario(
                    texto = if (estado.buscandoUbicacion) "Buscando ubicación..." else "Usar mi ubicación",
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
                    icono = IconosOjoD.Gps
                )

                Spacer(modifier = Modifier.height(14.dp))

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
            }

            // Con internet se puede elegir el punto en el mapa; sin internet solo se escriben coordenadas
            if (estadoUbicacion.hayInternet) {
                EtiquetaSeccion("Toca el mapa para fijar el punto")
                SelectorUbicacionMapa(
                    latitud = ValidadorParcela.parsearCoordenada(estado.latitud),
                    longitud = ValidadorParcela.parsearCoordenada(estado.longitud),
                    onUbicacionElegida = { punto -> viewModel.onMapaTocado(punto.latitude, punto.longitude) }
                )
            }

            estado.mensaje?.let {
                Insignia(
                    texto = it,
                    tipo = if (it.startsWith("Revisa") || it.startsWith("No se") || it.startsWith("Sin permiso"))
                        TipoEstado.ALERTA else TipoEstado.INFO,
                    icono = Icons.Default.Check
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            BotonPrincipal(
                texto = if (estado.guardando) "Guardando..." else "Guardar parcela",
                onClick = { viewModel.guardar(onGuardada) },
                habilitado = !estado.guardando
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}
