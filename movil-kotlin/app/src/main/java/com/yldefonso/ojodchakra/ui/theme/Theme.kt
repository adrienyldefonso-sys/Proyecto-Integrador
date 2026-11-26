package com.yldefonso.ojodchakra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// Sin color dinámico: la app siempre usa su paleta para mantener la identidad en ambos modos.
private val EsquemaClaro = lightColorScheme(
    primary = VerdeBosque,
    onPrimary = Color.White,
    primaryContainer = VerdeBosqueClaro,
    onPrimaryContainer = TierraOscura,
    secondary = CosechaDorada,
    onSecondary = Color.White,
    secondaryContainer = CosechaClara,
    onSecondaryContainer = Color(0xFF3D2A08),
    tertiary = Color(0xFF2F7F5F),
    onTertiary = Color.White,
    background = FondoClaro,
    onBackground = TextoClaro,
    surface = SuperficieClara,
    onSurface = TextoClaro,
    surfaceVariant = VerdeBosqueMuyClaro,
    onSurfaceVariant = TextoSecundarioClaro,
    outline = BordeClaro,
    outlineVariant = BordeSuaveClaro,
    error = Color(0xFFB3261E),
    onError = Color.White
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeMenta,
    onPrimary = Color(0xFF003915),
    primaryContainer = VerdeBosqueOscuro,
    onPrimaryContainer = VerdeBosqueClaro,
    secondary = CosechaOscura,
    onSecondary = Color(0xFF432D05),
    secondaryContainer = CosechaOscuraFondo,
    onSecondaryContainer = CosechaClara,
    tertiary = Color(0xFF7FCBA5),
    onTertiary = Color(0xFF003824),
    background = FondoOscuro,
    onBackground = TextoOscuro,
    surface = SuperficieOscura,
    onSurface = TextoOscuro,
    surfaceVariant = SuperficieElevadaOscura,
    onSurfaceVariant = TextoSecundarioOscuro,
    outline = BordeOscuro,
    outlineVariant = BordeSuaveOscuro,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun OjoDChakraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalColoresApp provides if (darkTheme) ColoresAppOscuros else ColoresAppClaros
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
            typography = Typography,
            content = content
        )
    }
}
