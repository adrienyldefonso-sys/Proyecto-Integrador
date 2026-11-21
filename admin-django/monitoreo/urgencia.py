"""HU-06 T2: nivel de urgencia de una parcela y agrupación por nivel."""
from datetime import timedelta

from .niveles import severidad_efectiva

ALTA = "ALTA"
MEDIA = "MEDIA"
SIN_ALERTA = "SIN_ALERTA"
NIVELES = (ALTA, MEDIA, SIN_ALERTA)

VENTANA_DIAS = 15
MINIMO_MEDIAS = 2


def calcular_urgencia(diagnosticos, ahora):
    """`diagnosticos` va del más reciente al más antiguo. Devuelve (nivel, motivo)."""
    if not diagnosticos:
        return SIN_ALERTA, "Sin diagnósticos registrados."

    ultimo = diagnosticos[0]
    severidad = severidad_efectiva(ultimo.plaga_detectada, ultimo.nivel_severidad)

    if severidad == "ALTA":
        return ALTA, f"Último diagnóstico: {ultimo.plaga_nombre} (severidad alta)."
    if severidad == "MEDIA":
        return MEDIA, f"Último diagnóstico: {ultimo.plaga_nombre} (severidad media)."

    desde = ahora - timedelta(days=VENTANA_DIAS)
    medias = sum(
        1
        for d in diagnosticos
        if d.fecha_captura >= desde
        and severidad_efectiva(d.plaga_detectada, d.nivel_severidad) == "MEDIA"
    )
    if medias >= MINIMO_MEDIAS:
        return MEDIA, f"{medias} diagnósticos de severidad media en los últimos {VENTANA_DIAS} días."

    return SIN_ALERTA, "El último diagnóstico no representa riesgo."


def agrupar_por_urgencia(parcelas, ahora):
    """Cada parcela trae sus diagnósticos en `diagnosticos_ordenados` (más reciente primero)."""
    grupos = {nivel: [] for nivel in NIVELES}
    for parcela in parcelas:
        diagnosticos = parcela.diagnosticos_ordenados
        nivel, motivo = calcular_urgencia(diagnosticos, ahora)
        ultimo = diagnosticos[0] if diagnosticos else None
        grupos[nivel].append(
            {"nivel": nivel, "motivo": motivo, "parcela": parcela, "ultimo": ultimo}
        )
    return grupos