from datetime import timedelta

from django.utils import timezone
from rest_framework.test import APITestCase

from .models import Diagnostico, Parcela, Usuario


class MetricasTests(APITestCase):
    def setUp(self):
        self.agronomo = Usuario.objects.create(
            username="agronomo1", rol=Usuario.Rol.AGRONOMO, is_staff=True
        )
        self.agricultor = Usuario.objects.create(
            username="juan", rol=Usuario.Rol.AGRICULTOR
        )
        self.parcela = Parcela.objects.create(usuario=self.agricultor, nombre="Lote norte")
        ahora = timezone.now()
        Diagnostico.objects.create(
            usuario=self.agricultor, parcela=self.parcela,
            plaga_detectada="Potato___healthy", confianza=0.9,
            fecha_captura=ahora - timedelta(days=20),
        )
        Diagnostico.objects.create(
            usuario=self.agricultor, parcela=self.parcela,
            plaga_detectada="Potato___Late_blight", confianza=0.8,
            fecha_captura=ahora - timedelta(hours=2),
            estado_revision=Diagnostico.EstadoRevision.SOLICITADA,
        )
        self.client.force_authenticate(self.agronomo)

    def test_solo_el_agronomo_ve_las_metricas(self):
        self.client.force_authenticate(self.agricultor)
        self.assertEqual(self.client.get("/api/metricas/").status_code, 403)

    def test_indicadores_y_distribucion(self):
        datos = self.client.get("/api/metricas/").json()
        self.assertEqual(datos["kpis"]["parcelas"], 1)
        self.assertEqual(datos["kpis"]["alerta_alta"], 1)
        self.assertEqual(datos["kpis"]["diagnosticos_recientes"], 2)
        self.assertEqual(datos["kpis"]["agricultores_activos"], 1)
        self.assertEqual(datos["kpis"]["revisiones_pendientes"], 1)
        self.assertAlmostEqual(datos["kpis"]["confianza_promedio"], 0.85, places=4)
        self.assertEqual(sum(p["total"] for p in datos["plagas"]), 2)
        self.assertEqual(datos["urgencia"]["ALTA"], 1)

    def test_ocho_semanas_y_actividad_reciente(self):
        datos = self.client.get("/api/metricas/").json()
        self.assertEqual(len(datos["semanas"]), 8)
        self.assertEqual(sum(s["total"] for s in datos["semanas"]), 2)
        self.assertEqual(len(datos["recientes"]), 2)
        self.assertEqual(datos["recientes"][0]["parcela"], "Lote norte")
