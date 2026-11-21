from django.db.models import Count, Prefetch
from django.utils import timezone
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Diagnostico, Parcela
from .permisos import EsAgronomo
from .ruta import ordenar_y_numerar
from .serializers import DiagnosticoSerializer, ParcelaSerializer
from .urgencia import NIVELES, agrupar_por_urgencia


class AlertasView(APIView):
    permission_classes = [EsAgronomo]

    def get(self, request):
        parcelas = (
            Parcela.objects.select_related("usuario")
            .annotate(total_diagnosticos=Count("diagnosticos"))
            .prefetch_related(
                Prefetch(
                    "diagnosticos",
                    queryset=Diagnostico.objects.order_by("-fecha_captura"),
                    to_attr="diagnosticos_ordenados",
                )
            )
        )
        grupos = ordenar_y_numerar(agrupar_por_urgencia(list(parcelas), timezone.now()))

        contexto = {"request": request}
        respuesta = {
            nivel: [
                {
                    "orden": item["orden"],
                    "nivel": item["nivel"],
                    "motivo": item["motivo"],
                    "parcela": ParcelaSerializer(item["parcela"], context=contexto).data,
                    "ultimo_diagnostico": (
                        DiagnosticoSerializer(item["ultimo"], context=contexto).data
                        if item["ultimo"]
                        else None
                    ),
                }
                for item in grupos[nivel]
            ]
            for nivel in NIVELES
        }
        resumen = {nivel: len(respuesta[nivel]) for nivel in NIVELES}
        resumen["total"] = sum(resumen.values())

        return Response({"resumen": resumen, "grupos": respuesta})