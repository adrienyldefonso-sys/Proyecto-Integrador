from datetime import timedelta

from django.contrib import admin
from django.test import RequestFactory, TestCase
from django.utils import timezone

from .admin import EventoAuditoriaAdmin
from .models import EventoAuditoria


class EventoAuditoriaTests(TestCase):
    def test_los_eventos_salen_del_mas_reciente_al_mas_antiguo(self):
        EventoAuditoria.objects.create(
            accion="VIEJO", entidad="parcela", entidad_id="a",
            fecha=timezone.now() - timedelta(days=2),
        )
        EventoAuditoria.objects.create(
            accion="NUEVO", entidad="parcela", entidad_id="b", fecha=timezone.now()
        )

        acciones = list(EventoAuditoria.objects.values_list("accion", flat=True))

        self.assertEqual(acciones, ["NUEVO", "VIEJO"])

    def test_el_panel_no_permite_agregar_modificar_ni_borrar(self):
        panel = EventoAuditoriaAdmin(EventoAuditoria, admin.site)
        peticion = RequestFactory().get("/")

        self.assertFalse(panel.has_add_permission(peticion))
        self.assertFalse(panel.has_change_permission(peticion))
        self.assertFalse(panel.has_delete_permission(peticion))