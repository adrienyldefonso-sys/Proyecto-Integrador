"""HU-06 T3: orden cronológico para planificar la ruta de visitas técnicas."""
from .urgencia import ALTA, MEDIA, SIN_ALERTA


def _clave(item):
    """Primero el diagnóstico más reciente; las parcelas sin diagnósticos, al final."""
    ultimo = item["ultimo"]
    if ultimo is None:
        return (1, 0, item["parcela"].nombre)
    return (0, -ultimo.fecha_captura.timestamp(), item["parcela"].nombre)


def ordenar_y_numerar(grupos):
    """Ordena cada grupo y numera la ruta: primero las ALTA y luego las MEDIA."""
    orden = 1
    for nivel in (ALTA, MEDIA, SIN_ALERTA):
        grupos[nivel].sort(key=_clave)
        for item in grupos[nivel]:
            if nivel == SIN_ALERTA:
                item["orden"] = None
            else:
                item["orden"] = orden
                orden += 1
    return grupos