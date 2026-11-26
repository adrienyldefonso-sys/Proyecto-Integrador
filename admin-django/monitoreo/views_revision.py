from django.shortcuts import get_object_or_404
from django.utils.decorators import method_decorator
from django.views.decorators.csrf import ensure_csrf_cookie
from rest_framework.exceptions import ValidationError
from rest_framework.generics import ListAPIView
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Diagnostico
from .permisos import EsAgronomo
from .revision import revisar
from .serializers import RevisionSerializer

E = Diagnostico.EstadoRevision

# Filtros del panel: pendientes (por defecto), ya revisadas o todas las que tienen revisión
FILTROS = {
    "pendientes": [E.SOLICITADA],
    "revisadas": [E.CONFIRMADA, E.CORREGIDA],
    "todas": [E.SOLICITADA, E.CONFIRMADA, E.CORREGIDA],
}


class RevisionesListView(ListAPIView):
    serializer_class = RevisionSerializer
    permission_classes = [EsAgronomo]

    def get_queryset(self):
        filtro = self.request.query_params.get("estado", "pendientes")
        if filtro not in FILTROS:
            raise ValidationError({"estado": "Usa pendientes, revisadas o todas."})
        return (
            Diagnostico.objects.select_related("usuario", "parcela")
            .filter(estado_revision__in=FILTROS[filtro])
            .order_by("-fecha_captura")
        )


class RevisarDiagnosticoView(APIView):
    permission_classes = [EsAgronomo]

    def post(self, request, diagnostico_id):
        diagnostico = get_object_or_404(
            Diagnostico.objects.select_related("usuario", "parcela"), pk=diagnostico_id
        )
        diagnostico = revisar(
            diagnostico,
            agronomo=request.user,
            decision=str(request.data.get("decision", "")).upper(),
            etiqueta=request.data.get("etiqueta_validada"),
            observacion=request.data.get("observacion", ""),
        )
        return Response(RevisionSerializer(diagnostico, context={"request": request}).data)


@method_decorator(ensure_csrf_cookie, name="dispatch")
class SesionView(APIView):
    """Entrega la cookie CSRF que el panel de React necesita para enviar cambios."""
    permission_classes = [AllowAny]

    def get(self, request):
        usuario = request.user
        autenticado = bool(usuario and usuario.is_authenticated)
        return Response({
            "autenticado": autenticado,
            "usuario": usuario.username if autenticado else None,
            "es_agronomo": autenticado and (
                usuario.is_superuser or getattr(usuario, "rol", "") == "AGRONOMO"
            ),
        })
