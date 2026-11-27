"""Resumen general del panel: indicadores agregados para la pantalla de inicio."""
from collections import defaultdict
from datetime import datetime, time, timedelta

from django.db.models import Avg, Count, Prefetch
from django.utils import timezone
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Diagnostico, Parcela, Usuario
from .permisos import EsAgronomo
from .urgencia import NIVELES, agrupar_por_urgencia

SEMANAS = 8
DIAS_RECIENTES = 30
ACTIVIDAD_RECIENTE = 5


class MetricasView(APIView):
    permission_classes = [EsAgronomo]

    def get(self, request):
        ahora = timezone.now()
        hoy = timezone.localdate(ahora)
        lunes_actual = hoy - timedelta(days=hoy.weekday())
        primer_lunes = lunes_actual - timedelta(weeks=SEMANAS - 1)

        parcelas = list(
            Parcela.objects.prefetch_related(
                Prefetch(
                    "diagnosticos",
                    queryset=Diagnostico.objects.order_by("-fecha_captura"),
                    to_attr="diagnosticos_ordenados",
                )
            )
        )
        grupos = agrupar_por_urgencia(parcelas, ahora)
        urgencia = {nivel: len(grupos[nivel]) for nivel in NIVELES}

        diagnosticos = Diagnostico.objects.all()
        confianza = diagnosticos.aggregate(promedio=Avg("confianza"))["promedio"]
        por_plaga = (
            diagnosticos.values("plaga_detectada")
            .annotate(total=Count("id"))
            .order_by("-total", "plaga_detectada")
        )

        # Un diagnóstico por semana (de lunes a domingo), contando desde el primer lunes de la ventana
        semanas = {primer_lunes + timedelta(weeks=i): 0 for i in range(SEMANAS)}
        fechas = Diagnostico.objects.filter(
            fecha_captura__gte=timezone.make_aware(datetime.combine(primer_lunes, time.min))
        ).values_list("fecha_captura", flat=True)
        for fecha in fechas:
            dia = timezone.localtime(fecha).date()
            inicio = dia - timedelta(days=dia.weekday())
            if inicio in semanas:
                semanas[inicio] += 1

        recientes = (
            Diagnostico.objects.select_related("parcela")
            .order_by("-fecha_captura")[:ACTIVIDAD_RECIENTE]
        )

        return Response(
            {
                "generado": ahora.isoformat(),
                "kpis": {
                    "parcelas": len(parcelas),
                    "alerta_alta": urgencia["ALTA"],
                    "diagnosticos_recientes": Diagnostico.objects.filter(
                        fecha_captura__gte=ahora - timedelta(days=DIAS_RECIENTES)
                    ).count(),
                    "agricultores_activos": Usuario.objects.filter(
                        rol=Usuario.Rol.AGRICULTOR, is_active=True
                    ).count(),
                    "revisiones_pendientes": Diagnostico.objects.filter(
                        estado_revision=Diagnostico.EstadoRevision.SOLICITADA
                    ).count(),
                    "confianza_promedio": confianza,
                },
                "urgencia": urgencia,
                "plagas": [
                    {"clave": fila["plaga_detectada"], "total": fila["total"]} for fila in por_plaga
                ],
                "semanas": [
                    {"inicio": inicio.isoformat(), "total": total}
                    for inicio, total in sorted(semanas.items())
                ],
                "recientes": [
                    {
                        "id": d.id,
                        "parcela": d.parcela.nombre if d.parcela else None,
                        "parcela_id": d.parcela_id,
                        "plaga": d.plaga_detectada,
                        "confianza": d.confianza,
                        "fecha": d.fecha_captura.isoformat(),
                    }
                    for d in recientes
                ],
            }
        )
