from django.db.models import Count, F, Max, Q
from django.shortcuts import get_object_or_404
from django.utils import timezone
from rest_framework.exceptions import ValidationError
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Diagnostico, Parcela, SesionMovil, Usuario
from .permisos import EsAgronomo

# Solo se gestionan cuentas de agricultores: las del agrónomo y del administrador no se pueden desactivar desde aquí.
MAX_INICIOS = 30
MAX_REPORTES = 10


def _cuentas_agricultores():
    """Agricultores con sus totales. Los contadores usan distinct porque hay varios joins."""
    ahora = timezone.now()
    return (
        Usuario.objects.filter(rol=Usuario.Rol.AGRICULTOR)
        .annotate(
            total_parcelas=Count("parcelas", distinct=True),
            total_reportes=Count("diagnosticos", distinct=True),
            sesiones_vigentes=Count(
                "sesiones_movil",
                filter=Q(sesiones_movil__expira__gt=ahora),
                distinct=True,
            ),
            ultimo_inicio=Max("inicios_sesion__fecha"),
        )
    )


def _serializar_cuenta(usuario):
    return {
        "id": usuario.id,
        "usuario": usuario.username,
        "nombre": usuario.first_name,
        "telefono": usuario.telefono or "",
        "activo": usuario.is_active,
        "fecha_registro": usuario.date_joined,
        "ultimo_inicio": usuario.ultimo_inicio,
        "sesiones_vigentes": usuario.sesiones_vigentes,
        "parcelas": usuario.total_parcelas,
        "reportes": usuario.total_reportes,
    }


class UsuariosListView(APIView):
    permission_classes = [EsAgronomo]

    def get(self, request):
        cuentas = _cuentas_agricultores().order_by(
            F("ultimo_inicio").desc(nulls_last=True), "username"
        )
        return Response([_serializar_cuenta(u) for u in cuentas])


class UsuarioDetalleView(APIView):
    permission_classes = [EsAgronomo]

    def get(self, request, usuario_id):
        cuenta = get_object_or_404(_cuentas_agricultores(), pk=usuario_id)
        datos = _serializar_cuenta(cuenta)
        datos["inicios"] = [
            {"fecha": inicio.fecha}
            for inicio in cuenta.inicios_sesion.order_by("-fecha")[:MAX_INICIOS]
        ]
        datos["lista_parcelas"] = [
            {
                "id": parcela.id,
                "nombre": parcela.nombre,
                "cultivo": parcela.cultivo or "",
                "fecha_creacion": parcela.fecha_creacion,
            }
            for parcela in Parcela.objects.filter(usuario=cuenta).order_by("nombre")
        ]
        datos["ultimos_reportes"] = [
            {
                "id": diagnostico.id,
                "fecha_captura": diagnostico.fecha_captura,
                "plaga_detectada": diagnostico.plaga_detectada,
                "estado_revision": diagnostico.estado_revision,
                "parcela": diagnostico.parcela.nombre if diagnostico.parcela_id else "",
            }
            for diagnostico in Diagnostico.objects.select_related("parcela")
            .filter(usuario=cuenta)
            .order_by("-fecha_captura")[:MAX_REPORTES]
        ]
        return Response(datos)


class UsuarioEstadoView(APIView):
    """Activa o desactiva la cuenta. Al desactivarla también se cierran sus sesiones."""
    permission_classes = [EsAgronomo]

    def post(self, request, usuario_id):
        cuenta = get_object_or_404(Usuario.objects.filter(rol=Usuario.Rol.AGRICULTOR), pk=usuario_id)
        activo = request.data.get("activo")
        if not isinstance(activo, bool):
            raise ValidationError({"activo": "Indica true o false."})
        cuenta.is_active = activo
        cuenta.save(update_fields=["is_active"])
        if not activo:
            SesionMovil.objects.filter(usuario=cuenta).delete()
        return Response(_serializar_cuenta(_cuentas_agricultores().get(pk=cuenta.pk)))


class UsuarioCerrarSesionesView(APIView):
    """Cierra todas las sesiones de la app móvil de la cuenta. Tendrá que volver a iniciar sesión."""
    permission_classes = [EsAgronomo]

    def post(self, request, usuario_id):
        cuenta = get_object_or_404(Usuario.objects.filter(rol=Usuario.Rol.AGRICULTOR), pk=usuario_id)
        cerradas, _ = SesionMovil.objects.filter(usuario=cuenta).delete()
        return Response({"cerradas": cerradas})
