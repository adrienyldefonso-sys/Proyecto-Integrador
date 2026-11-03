from django.contrib import admin
from django.contrib.auth.admin import UserAdmin
from .models import Usuario, Parcela, Diagnostico

@admin.register(Usuario)
class UsuarioAdmin(UserAdmin):
    list_display = ("username", "email", "first_name", "last_name", "rol", "is_staff")
    list_filter = ("rol", "is_staff")

@admin.register(Parcela)
class ParcelaAdmin(admin.ModelAdmin):
    list_display = ("nombre", "usuario", "cultivo", "fecha_creacion")
    search_fields = ("nombre", "usuario__username", "cultivo")
    list_filter = ("fecha_creacion",)

@admin.register(Diagnostico)
class DiagnosticoAdmin(admin.ModelAdmin):
    list_display = ("plaga_detectada", "usuario", "parcela", "confianza", "nivel_severidad", "fecha_captura")
    search_fields = ("plaga_detectada", "usuario__username", "parcela__nombre")
    list_filter = ("nivel_severidad", "fecha_captura")