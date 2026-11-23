from datetime import timedelta

from django.urls import reverse
from django.utils import timezone
from rest_framework.test import APITestCase

from .models import Diagnostico, Parcela, Usuario


class ActualizacionDelHistorialTests(APITestCase):
    def setUp(self):
        self.agronomo = Usuario.objects.create_user(
            username="agro", password="x", rol=Usuario.Rol.AGRONOMO
        )
        self.agricultor = Usuario.objects.create_user(username="agri", password="x")
        self.parcela = Parcela.objects.create(
            usuario=self.agricultor, nombre="Finca Norte", cultivo="Papa",
            latitud=-12.04, longitud=-77.04,
        )
        self.client.force_authenticate(self.agronomo)

    def _diagnostico(self, plaga, dias, foto=None):
        return Diagnostico.objects.create(
            usuario=self.agricultor,
            parcela=self.parcela,
            plaga_detectada=plaga,
            confianza=90.0,
            foto=foto,
            fecha_captura=timezone.now() - timedelta(days=dias),
        )

    def _historial(self):
        return self.client.get(
            reverse("historial-parcela", kwargs={"parcela_id": self.parcela.id})
        )

    def _total_en_la_lista(self):
        respuesta = self.client.get(reverse("parcelas"))
        fila = next(p for p in respuesta.data if p["id"] == str(self.parcela.id))
        return fila["total_diagnosticos"]

    def test_un_diagnostico_nuevo_aparece_primero_y_actualiza_el_resumen(self):
        self._diagnostico("Potato___healthy", 5)
        self.assertEqual(self._historial().data["resumen"]["total"], 1)

        nuevo = self._diagnostico("Potato___Late_blight", 0)

        despues = self._historial().data
        self.assertEqual(despues["resumen"]["total"], 2)
        self.assertEqual(despues["diagnosticos"][0]["id"], str(nuevo.id))
        self.assertEqual(
            despues["resumen"]["por_plaga"],
            {"Potato___healthy": 1, "Potato___Late_blight": 1},
        )

    def test_la_lista_de_parcelas_cuenta_el_diagnostico_nuevo(self):
        self._diagnostico("Potato___healthy", 3)
        self.assertEqual(self._total_en_la_lista(), 1)

        self._diagnostico("Potato___Late_blight", 0)

        self.assertEqual(self._total_en_la_lista(), 2)

    def test_el_diagnostico_enviado_por_la_app_trae_la_url_de_su_foto(self):
        self._diagnostico("Potato___Late_blight", 0, foto="diagnosticos/abc.jpg")

        item = self._historial().data["diagnosticos"][0]

        self.assertTrue(item["foto_url"].endswith("/media/diagnosticos/abc.jpg"))

    def test_un_diagnostico_sin_foto_devuelve_la_url_nula(self):
        self._diagnostico("Potato___healthy", 0)

        item = self._historial().data["diagnosticos"][0]

        self.assertIsNone(item["foto_url"])