package com.yldefonso.ojodchakra.screens.result

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.yldefonso.ojodchakra.screens.camera.CameraViewModel

@Composable
fun ResultScreen(
    viewModel: CameraViewModel,
    onScanAgain: () -> Unit
) {
    val bitmap = viewModel.capturedBitmap.value
    val result = viewModel.result.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Foto capturada",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (result != null) {
            Text(text = "Diagnóstico: ${result.label}")
            Text(text = "Confianza: ${"%.2f".format(result.confidence)}%")
        } else {
            Text(text = "No se pudo obtener un diagnóstico")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.reset()
                onScanAgain()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Escanear otra vez")
        }
    }
}