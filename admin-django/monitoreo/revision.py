from django.utils import timezone
from rest_framework.exceptions import ValidationError

from .models import MAPA_PLAGAS, Diagnostico, EventoAuditoria

CONFIRMAR = "CONFIRMAR"
CORREGIR = "CORREGIR"
MAX_OBSERVACION = 2000


def revisar(diagnostico, agronomo, decision, etiqueta=None, observacion=""):
    """Aplica la decisión del agrónomo y deja rastro en la auditoría."""
    if decision not in (CONFIRMAR, CORREGIR):
        raise ValidationError({"decision": "Usa CONFIRMAR o CORREGIR."})

    observacion = (observacion or "").strip()
    if len(observacion) > MAX_OBSERVACION:
        raise ValidationError({"observacion": "Máximo 2000 caracteres."})

    if decision == CONFIRMAR:
        etiqueta_final = diagnostico.plaga_detectada
        estado = Diagnostico.EstadoRevision.CONFIRMADA
    else:
        etiqueta_final = (etiqueta or "").strip()
        if etiqueta_final not in MAPA_PLAGAS:
            raise ValidationError({"etiqueta_validada": "Elige una plaga válida."})
        if etiqueta_final == diagnostico.plaga_detectada:
            raise ValidationError(
                {"etiqueta_validada": "Si la plaga coincide con el resultado, usa Confirmar."}
            )
        estado = Diagnostico.EstadoRevision.CORREGIDA

    diagnostico.estado_revision = estado
    diagnostico.etiqueta_validada = etiqueta_final
    diagnostico.observacion_agronomo = observacion
    diagnostico.fecha_revision = timezone.now()
    diagnostico.revisado_por = agronomo
    diagnostico.save(update_fields=[
        "estado_revision",
        "etiqueta_validada",
        "observacion_agronomo",
        "fecha_revision",
        "revisado_por",
    ])

    EventoAuditoria.objects.create(
        accion="REVISION_AGRONOMO",
        entidad="diagnostico",
        entidad_id=diagnostico.id,
        usuario_id=str(agronomo.id),
        origen="django",
        detalle=f"{estado}: {etiqueta_final}",
    )
    return diagnostico
