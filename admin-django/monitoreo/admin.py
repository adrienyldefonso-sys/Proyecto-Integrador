
from django.contrib import admin
from django.contrib.auth.admin import UserAdmin
from .models import Usuario, Parcela, Diagnostico, EventoAuditoria


@admin.register(Usuario)
class UsuarioAdmin(UserAdmin):
    list_display = (
        "username", "email", "first_name",
        "last_name", "rol", "is_staff"
    )
    list_filter = ("rol", "is_staff")


@admin.register(Parcela)
class ParcelaAdmin(admin.ModelAdmin):
    list_display = (
        "nombre", "usuario", "cultivo", "fecha_creacion"
    )
    search_fields = (
        "nombre", "usuario__username", "cultivo"
    )
    list_filter = ("fecha_creacion",)


@admin.register(Diagnostico)
class DiagnosticoAdmin(admin.ModelAdmin):
    list_display = (
        "plaga_detectada", "usuario", "parcela",
        "confianza", "nivel_severidad", "fecha_captura",
        "estado_revision"
    )
    search_fields = (
        "plaga_detectada",
        "usuario__username",
        "parcela__nombre"
    )
    list_filter = ("nivel_severidad", "estado_revision", "fecha_captura")


@admin.register(EventoAuditoria)
class EventoAuditoriaAdmin(admin.ModelAdmin):
    list_display = (
        "fecha", "accion", "entidad",
        "entidad_id", "usuario_id", "origen"
    )
    list_filter = ("accion", "origen")
    search_fields = ("entidad_id", "usuario_id")

    def has_add_permission(self, request):
        return False

    def has_change_permission(self, request, obj=None):
        return False

    def has_delete_permission(self, request, obj=None):
        return False
