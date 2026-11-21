"""HU-06 T1: niveles de severidad según el tipo de plaga."""

SEVERIDADES_VALIDAS = ("ALTA", "MEDIA", "BAJA", "NINGUNA")

# Misma tabla que el catálogo de recomendaciones de la app móvil
SEVERIDAD_POR_PLAGA = {
    "Potato___Late_blight": "ALTA",
    "Potato___Early_blight": "MEDIA",
    "Potato___healthy": "NINGUNA",
}


def severidad_efectiva(plaga_detectada, nivel_guardado):
    """Usa la severidad guardada; si está vacía o no es válida, la deduce de la plaga."""
    nivel = (nivel_guardado or "").strip().upper()
    if nivel in SEVERIDADES_VALIDAS:
        return nivel
    return SEVERIDAD_POR_PLAGA.get(plaga_detectada, "NINGUNA")