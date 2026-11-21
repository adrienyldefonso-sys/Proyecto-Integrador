# HU-06: Reglas del tablero de alertas

## Severidad según el tipo de plaga (T1)

| Etiqueta del modelo | Nombre | Severidad |
|---|---|---|
| Potato___Late_blight | Tizón tardío | ALTA |
| Potato___Early_blight | Tizón temprano | MEDIA |
| Potato___healthy | Planta sana | NINGUNA |

Si el diagnóstico trae una severidad válida (ALTA, MEDIA, BAJA o NINGUNA), se usa esa.
Si viene vacía, se deduce de la plaga. Una plaga desconocida sin severidad no genera alerta.

## Nivel de urgencia de una parcela (T2)

Las reglas se evalúan en este orden:

| Orden | Nivel | Regla |
|---|---|---|
| 1 | ALTA | El último diagnóstico es de severidad ALTA |
| 2 | MEDIA | El último es MEDIA, o hay 2 o más diagnósticos MEDIA en los últimos 15 días |
| 3 | SIN_ALERTA | Cualquier otro caso, incluidas las parcelas sin diagnósticos |

Si el último diagnóstico es sano pero hubo 2 o más MEDIA en 15 días, la parcela queda
en MEDIA, porque la regla 2 se evalúa antes que la 3.

## Ruta de visitas (T3)

- Primero las parcelas ALTA y luego las MEDIA. Las SIN_ALERTA no entran en la ruta.
- Dentro de cada nivel va primero la parcela con el diagnóstico más reciente.
  Las parcelas sin diagnósticos quedan al final de su grupo.
- Cada parcela de la ruta lleva un número de orden (1, 2, 3...).

## Endpoint

GET /api/alertas/ (solo rol agrónomo)

    {
      "resumen": {"ALTA": 1, "MEDIA": 1, "SIN_ALERTA": 2, "total": 4},
      "grupos": {
        "ALTA": [{"orden": 1, "nivel": "ALTA", "motivo": "...", "parcela": {...}, "ultimo_diagnostico": {...}}],
        "MEDIA": [],
        "SIN_ALERTA": []
      }
    }
