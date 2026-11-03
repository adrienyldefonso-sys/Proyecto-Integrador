from datetime import datetime, time

from django.db.models import Count, Q
from django.shortcuts import get_object_or_404
from django.utils import timezone
from django.utils.dateparse import parse_date
from rest_framework.exceptions import ValidationError
from rest_framework.generics import ListAPIView
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Parcela
from .serializers import DiagnosticoSerializer, ParcelaSerializer


def _limite(texto, nombre, final):
    try:
        fecha = parse_date(texto)
    except ValueError:
        fecha = None
    if fecha is None:
        raise ValidationError({nombre: "Usa el formato AAAA-MM-DD."})
    hora = time.max if final else time.min
    return timezone.make_aware(datetime.combine(fecha, hora))


class ParcelaListView(ListAPIView):
    serializer_class = ParcelaSerializer

    def get_queryset(self):
        return (
            Parcela.objects.select_related("usuario")
            .annotate(total_diagnosticos=Count("diagnosticos"))
            .order_by("nombre")
        )


class HistorialParcelaView(APIView):
    def get(self, request, parcela_id):
        # Permite buscar la parcela con o sin guiones en la URL
        parcela_id_clean = str(parcela_id).replace("-", "")
        parcela = get_object_or_404(
            Parcela.objects.select_related("usuario"),
            Q(pk=parcela_id) | Q(pk=parcela_id_clean),
        )

        # Ordenamiento descendente (del más reciente al más antiguo)
        diagnosticos = parcela.diagnosticos.order_by("-fecha_captura")

        desde = request.query_params.get("desde")
        hasta = request.query_params.get("hasta")
        if desde:
            diagnosticos = diagnosticos.filter(
                fecha_captura__gte=_limite(desde, "desde", False)
            )
        if hasta:
            diagnosticos = diagnosticos.filter(
                fecha_captura__lte=_limite(hasta, "hasta", True)
            )

        contexto = {"request": request}
        datos = DiagnosticoSerializer(diagnosticos, many=True, context=contexto).data
        por_plaga = {
            fila["plaga_detectada"]: fila["total"]
            for fila in diagnosticos.order_by()
            .values("plaga_detectada")
            .annotate(total=Count("id"))
        }

        return Response(
            {
                "parcela": ParcelaSerializer(parcela, context=contexto).data,
                "resumen": {
                    "total": len(datos),
                    "ultima_fecha": datos[0]["fecha_captura"] if datos else None,
                    "por_plaga": por_plaga,
                },
                "diagnosticos": datos,
            }
        )