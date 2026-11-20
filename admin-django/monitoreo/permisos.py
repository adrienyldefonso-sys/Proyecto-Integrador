from rest_framework.permissions import BasePermission

from .models import Usuario


class EsAgronomo(BasePermission):
    message = "Solo un agrónomo puede consultar el historial de las parcelas."

    def has_permission(self, request, view):
        usuario = request.user
        if not (usuario and usuario.is_authenticated):
            return False
        return usuario.is_superuser or usuario.rol == Usuario.Rol.AGRONOMO