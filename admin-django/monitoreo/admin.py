from django.contrib import admin
from django.contrib.auth.admin import UserAdmin

from .models import Diagnostico, Parcela, Usuario


@admin.register(Usuario)
class UsuarioAdmin(UserAdmin):
    fieldsets = UserAdmin.fieldsets + (("Datos de OjoDChakra", {"fields": ("celular", "rol")}),)
    add_fieldsets = UserAdmin.add_fieldsets + (
        ("Datos de OjoDChakra", {"fields": ("celular", "rol")}),
    )
    list_display = ("username", "first_name", "last_name", "rol", "is_active")
    list_filter = ("rol", "is_active")


@admin.register(Parcela)
class ParcelaAdmin(admin.ModelAdmin):
    list_display = ("nombre", "cultivo", "usuario", "fecha_registro")
    list_filter = ("cultivo",)
    search_fields = ("nombre", "referencia", "usuario__username")


@admin.register(Diagnostico)
class DiagnosticoAdmin(admin.ModelAdmin):
    list_display = ("fecha_captura", "parcela", "plaga_detectada", "confianza", "nivel_severidad")
    list_filter = ("nivel_severidad", "plaga_detectada")
    date_hierarchy = "fecha_captura"