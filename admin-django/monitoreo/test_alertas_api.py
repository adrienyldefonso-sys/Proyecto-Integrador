from datetime import timedelta

from django.urls import reverse
from django.utils import timezone
from rest_framework.test import APITestCase

from .models import Diagnostico, Parcela, Usuario


class AlertasApiTests(APITestCase):
    def setUp(self):
        self.agronomo = Usuario.objects.create_user(
            username="agro", password="x", rol=Usuario.Rol.AGRONOMO
        )
        self.agricultor = Usuario.objects.create_user(username="agri", password="x")
        self.client.force_authenticate(self.agronomo)

    def _parcela(self, nombre):
        return Parcela.objects.create(
            usuario=self.agricultor, nombre=nombre, cultivo="Papa", latitud=-12.0, longitud=-77.0
        )

    def _diagnostico(self, parcela, plaga, dias):
        return Diagnostico.objects.create(
            usuario=self.agricultor,
            parcela=parcela,
            plaga_detectada=plaga,
            confianza=90.0,
            fecha_captura=timezone.now() - timedelta(days=dias),
        )

    def _alertas(self):
        return self.client.get(reverse("alertas"))

    def test_agrupa_las_parcelas_por_urgencia(self):
        self._diagnostico(self._parcela("Alta"), "Potato___Late_blight", 1)
        self._diagnostico(self._parcela("Media"), "Potato___Early_blight", 1)
        self._diagnostico(self._parcela("Sana"), "Potato___healthy", 1)
        self._parcela("Vacia")

        r = self._alertas()

        self.assertEqual(r.status_code, 200)
        self.assertEqual(
            r.data["resumen"], {"ALTA": 1, "MEDIA": 1, "SIN_ALERTA": 2, "total": 4}
        )

    def test_la_ruta_pone_primero_las_alta_mas_recientes(self):
        self._diagnostico(self._parcela("Media"), "Potato___Early_blight", 0)
        self._diagnostico(self._parcela("AltaVieja"), "Potato___Late_blight", 5)
        self._diagnostico(self._parcela("AltaNueva"), "Potato___Late_blight", 1)

        r = self._alertas()

        ruta = [
            (g["orden"], g["parcela"]["nombre"])
            for nivel in ("ALTA", "MEDIA")
            for g in r.data["grupos"][nivel]
        ]
        self.assertEqual(ruta, [(1, "AltaNueva"), (2, "AltaVieja"), (3, "Media")])

    def test_sin_alerta_no_tiene_numero_de_ruta(self):
        self._diagnostico(self._parcela("Sana"), "Potato___healthy", 1)

        r = self._alertas()

        self.assertIsNone(r.data["grupos"]["SIN_ALERTA"][0]["orden"])

    def test_el_tablero_se_actualiza_con_un_diagnostico_nuevo(self):
        parcela = self._parcela("Finca")
        self._diagnostico(parcela, "Potato___healthy", 2)
        self.assertEqual(self._alertas().data["resumen"]["ALTA"], 0)

        self._diagnostico(parcela, "Potato___Late_blight", 0)

        self.assertEqual(self._alertas().data["resumen"]["ALTA"], 1)

    def test_agricultor_no_puede_ver_las_alertas(self):
        self.client.force_authenticate(self.agricultor)
        self.assertEqual(self._alertas().status_code, 403)

    def test_sin_sesion_no_puede_ver_las_alertas(self):
        self.client.force_authenticate(user=None)
        self.assertIn(self._alertas().status_code, (401, 403))