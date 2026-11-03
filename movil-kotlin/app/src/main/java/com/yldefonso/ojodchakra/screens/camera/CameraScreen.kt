package com.yldefonso.ojodchakra.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yldefonso.ojodchakra.ml.PlagaClassifier
import java.util.concurrent.Executors

// Paleta de Colores Institucional OjoDChakra
private val VerdePrincipal = Color(0xFF2E6A38)
private val VerdeMenta = Color(0xFF10B981)
private val FondoPantalla = Color(0xFFFAFCFA)
private val SuperficieTarjeta = Color(0xFFFFFFFF)
private val VerdeSubtil = Color(0xFFEBF3EC)
private val TextoOscuro = Color(0xFF1B2E21)
private val TextoMuted = Color(0xFF5A7060)

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onDiagnosisReady: () -> Unit
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasPermission) {
            // 1. VISTA PREVIA DE LA CÁMARA (CAMERAX)
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

            // 2. OVERLAY / RECUADRO DE ENFOQUE (ESCÁNER)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Marco de enfoque central con esquinas redondeadas y borde menta
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            width = 2.5.dp,
                            color = VerdeMenta,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .background(Color.Black.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusWeak,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            // 3. BARRA SUPERIOR INFORMATIVA
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = VerdeMenta,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Coloca la hoja en el centro",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 4. DISPARADOR FLOTANTE TIPO CÁMARA PROFESIONAL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Botón circular obturador
                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(elevation = 12.dp, shape = CircleShape, spotColor = VerdePrincipal)
                            .clip(CircleShape)
                            .clickable(enabled = !isCapturing) {
                                val capture = imageCapture ?: return@clickable
                                isCapturing = true

                                capture.takePicture(
                                    cameraExecutor,
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(image: ImageProxy) {
                                            val bitmap = image.toBitmap()
                                            image.close()

                                            val classifier = PlagaClassifier(context)
                                            val result = classifier.classify(bitmap)

                                            mainExecutor.execute {
                                                viewModel.onPhotoCaptured(bitmap)
                                                viewModel.onResultReady(result)
                                                onDiagnosisReady()
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
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.25f),
                        border = BorderStroke(3.dp, Color.White)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(6.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                shape = CircleShape,
                                color = VerdePrincipal
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isCapturing) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(28.dp),
                                            strokeWidth = 3.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Escanear cultivo",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isCapturing) "Procesando muestra..." else "Toca para escanear",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

        } else {
            // 5. PANTALLA DE SOLICITUD DE PERMISO ESTILIZADA
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FondoPantalla)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = SuperficieTarjeta,
                    border = BorderStroke(1.dp, VerdePrincipal.copy(alpha = 0.2f)),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape,
                            color = VerdeSubtil
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Camera,
                                    contentDescription = null,
                                    tint = VerdePrincipal,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Permiso de cámara requerido",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextoOscuro,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "OjoDChakra necesita acceso a la cámara para tomar fotos de las hojas y diagnosticar posibles plagas en tiempo real.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextoMuted,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerdePrincipal,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Conceder permiso",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}