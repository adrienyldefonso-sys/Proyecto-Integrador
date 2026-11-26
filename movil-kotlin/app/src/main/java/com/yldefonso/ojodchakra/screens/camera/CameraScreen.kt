package com.yldefonso.ojodchakra.screens.camera

import com.yldefonso.ojodchakra.ui.iconos.IconosOjoD
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yldefonso.ojodchakra.ml.CalidadFoto
import com.yldefonso.ojodchakra.ml.DecisionHoja
import com.yldefonso.ojodchakra.ml.PlagaClassifier
import com.yldefonso.ojodchakra.ui.componentes.BotonPrincipal
import com.yldefonso.ojodchakra.ui.componentes.EncabezadoPantalla
import com.yldefonso.ojodchakra.ui.componentes.IconoContenedor
import com.yldefonso.ojodchakra.ui.componentes.Tarjeta
import com.yldefonso.ojodchakra.ui.theme.LocalColoresApp
import java.util.concurrent.Executors

/** Cámara de escaneo: vista previa a pantalla completa con guía de encuadre y disparador. */
@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onDiagnosisReady: () -> Unit,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasPermission) {
        PermisoCamara(
            onBack = onBack,
            onConceder = { permissionLauncher.launch(Manifest.permission.CAMERA) }
        )
        return
    }

    val colores = LocalColoresApp.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Vista previa de la cámara (CameraX)
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    imageCapture = capture

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // 2. Guía de encuadre: cuatro esquinas, sin marco de color que tape la hoja
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GuiaEncuadre(color = colores.bandaAcento)
        }

        // 3. Barra superior: volver y parcela elegida
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Text(
                    text = viewModel.parcelaNombre.value?.let { "Parcela: $it" } ?: "Sin parcela",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        // 4. Panel inferior: avisos y disparador
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.Black.copy(alpha = 0.55f))
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            viewModel.mensajeRechazo.value?.let { mensaje ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = colores.alertaFondo
                ) {
                    Text(
                        text = mensaje,
                        color = colores.alertaTexto,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            Box(
                modifier = Modifier
                    .size(82.dp)
                    .border(3.dp, Color.White, CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(enabled = !isCapturing) {
                        val capture = imageCapture ?: return@clickable
                        isCapturing = true

                        capture.takePicture(
                            cameraExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val bitmap = image.toBitmap()
                                    image.close()

                                    // Control de calidad: una foto borrosa u oscura no se analiza
                                    CalidadFoto.evaluar(bitmap)?.let { problema ->
                                        mainExecutor.execute {
                                            viewModel.onRechazada(problema.mensaje)
                                            isCapturing = false
                                        }
                                        return
                                    }

                                    val classifier = PlagaClassifier(context)
                                    val resultado = classifier.classify(bitmap)

                                    mainExecutor.execute {
                                        when (val veredicto = DecisionHoja.evaluar(resultado)) {
                                            is DecisionHoja.Veredicto.Aceptada -> {
                                                viewModel.limpiarRechazo()
                                                viewModel.onPhotoCaptured(bitmap)
                                                viewModel.onResultReady(veredicto.resultado)
                                                onDiagnosisReady()
                                            }
                                            is DecisionHoja.Veredicto.Rechazada ->
                                                viewModel.onRechazada(DecisionHoja.mensaje(veredicto.motivo))
                                        }
                                        isCapturing = false
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    exception.printStackTrace()
                                    mainExecutor.execute {
                                        isCapturing = false
                                    }
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(30.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = IconosOjoD.Escanear,
                        contentDescription = "Escanear cultivo",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isCapturing) "Procesando muestra..." else "Centra una hoja y toca el botón",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Esquinas de guía para encuadrar la hoja. */
@Composable
private fun GuiaEncuadre(color: Color) {
    Canvas(modifier = Modifier.size(270.dp)) {
        val largo = size.width * 0.18f
        val grosor = 4.dp.toPx()
        val ancho = size.width
        val alto = size.height

        fun linea(x1: Float, y1: Float, x2: Float, y2: Float) {
            drawLine(
                color = color,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = grosor,
                cap = StrokeCap.Round
            )
        }

        linea(0f, 0f, largo, 0f)
        linea(0f, 0f, 0f, largo)
        linea(ancho, 0f, ancho - largo, 0f)
        linea(ancho, 0f, ancho, largo)
        linea(0f, alto, largo, alto)
        linea(0f, alto, 0f, alto - largo)
        linea(ancho, alto, ancho - largo, alto)
        linea(ancho, alto, ancho, alto - largo)
    }
}

/** Pantalla cuando la app todavía no tiene permiso de cámara. */
@Composable
private fun PermisoCamara(onBack: () -> Unit, onConceder: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EncabezadoPantalla(
            titulo = "Escanear",
            subtitulo = "Permiso de cámara",
            onVolver = onBack
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Tarjeta(padding = 24.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconoContenedor(icono = IconosOjoD.Escanear, tamano = 72.dp)
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Necesitamos acceso a la cámara",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "OjoDChakra usa la cámara solo para fotografiar hojas y diagnosticar plagas. Las fotos se guardan en tu celular.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    BotonPrincipal(texto = "Conceder permiso", onClick = onConceder)
                }
            }
        }
    }
}
