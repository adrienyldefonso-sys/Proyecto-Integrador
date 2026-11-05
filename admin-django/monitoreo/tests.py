import shutil
import tempfile
import uuid
from datetime import timedelta

from django.core.files.uploadedfile import SimpleUploadedFile
from django.test import override_settings
from django.urls import reverse
from django.utils import timezone
from rest_framework.test import APITestCase

from .models import Diagnostico, Parcela, Usuario

MEDIA_TEMPORAL = tempfile.mkdtemp()


@override_settings(MEDIA_ROOT=MEDIA_TEMPORAL)
class HistorialParcelaTests(APITestCase):
    @classmethod
    def tearDownClass(cls):
        shutil.rmtree(MEDIA_TEMPORAL, ignore_errors=True)
        super().tearDownClass()

    def setUp(self):
        self.agronomo = Usuario.objects.create_user(
            username="agro", password="x", rol=Usuario.Rol.AGRONOMO
        )
        self.agricultor = Usuario.objects.create_user(username="agri", password="x")
        self.parcela = Parcela.objects.create(
            usuario=self.agricultor, nombre="Finca Norte", cultivo="Papa",
            latitud=-12.04, longitud=-77.04,
        )
        self.otra = Parcela.objects.create(
            usuario=self.agricultor, nombre="Finca Sur", cultivo="Papa",
            latitud=-12.05, longitud=-77.05,
        )
        self.client.force_authenticate(self.agronomo)

    def _diagnostico(self, parcela, plaga, dias, severidad=""):
        return Diagnostico.objects.create(
            usuario=self.agricultor,
            parcela=parcela,
            foto=SimpleUploadedFile("t.jpg", b"x", content_type="image/jpeg"),
            plaga_detectada=plaga,
            confianza=90.0,
            nivel_severidad=severidad,
            fecha_captura=timezone.now() - timedelta(days=dias),
        )

    def _url(self, parcela):
        return reverse("historial-parcela", kwargs={"parcela_id": parcela.id})

    def test_historial_viene_del_mas_reciente_al_mas_antiguo(self):
        viejo = self._diagnostico(self.parcela, "Potato___Late_blight", 10)
        nuevo = self._diagnostico(self.parcela, "Potato___Early_blight", 1)
        medio = self._diagnostico(self.parcela, "Potato___healthy", 5)

        r = self.client.get(self._url(self.parcela))

        self.assertEqual(r.status_code, 200)
        ids = [d["id"] for d in r.data["diagnosticos"]]
        self.assertEqual(ids, [str(nuevo.id), str(medio.id), str(viejo.id)])

    def test_cada_diagnostico_trae_foto_y_nombre_en_espanol(self):
        self._diagnostico(self.parcela, "Potato___Late_blight", 1)

        r = self.client.get(self._url(self.parcela))

        d = r.data["diagnosticos"][0]
        self.assertTrue(d["foto_url"].startswith("http"))
        self.assertEqual(d["plaga_nombre"], "Tizón tardío")

    def test_resumen_cuenta_total_y_por_plaga(self):
        self._diagnostico(self.parcela, "Potato___Late_blight", 1)
        self._diagnostico(self.parcela, "Potato___Late_blight", 2)
        self._diagnostico(self.parcela, "Potato___healthy", 3)

        r = self.client.get(self._url(self.parcela))

        self.assertEqual(r.data["resumen"]["total"], 3)
        self.assertEqual(
            r.data["resumen"]["por_plaga"],
            {"Potato___Late_blight": 2, "Potato___healthy": 1},
        )

    def test_no_mezcla_diagnosticos_de_otra_parcela(self):
        self._diagnostico(self.parcela, "Potato___healthy", 1)
        self._diagnostico(self.otra, "Potato___Late_blight", 1)

        r = self.client.get(self._url(self.parcela))

        self.assertEqual(len(r.data["diagnosticos"]), 1)

    def test_filtro_desde_excluye_los_mas_antiguos(self):
        self._diagnostico(self.parcela, "Potato___healthy", 20)
        reciente = self._diagnostico(self.parcela, "Potato___Late_blight", 2)
        desde = (timezone.localdate() - timedelta(days=5)).isoformat()

        r = self.client.get(self._url(self.parcela), {"desde": desde})

        self.assertEqual([d["id"] for d in r.data["diagnosticos"]], [str(reciente.id)])

    def test_fecha_invalida_devuelve_400(self):
        r = self.client.get(self._url(self.parcela), {"desde": "ayer"})
        self.assertEqual(r.status_code, 400)

    def test_parcela_inexistente_devuelve_404(self):
        r = self.client.get(
            reverse("historial-parcela", kwargs={"parcela_id": uuid.uuid4()})
        )
        self.assertEqual(r.status_code, 404)

    def test_id_que_no_es_uuid_devuelve_404(self):
        r = self.client.get("/api/parcelas/no-es-un-uuid/historial/")
        self.assertEqual(r.status_code, 404)

    def test_sin_sesion_no_se_puede_consultar(self):
        self.client.force_authenticate(user=None)
        r = self.client.get(self._url(self.parcela))
        self.assertIn(r.status_code, (401, 403))

    def test_lista_de_parcelas_trae_el_total_de_diagnosticos(self):
        self._diagnostico(self.parcela, "Potato___healthy", 1)
        self._diagnostico(self.parcela, "Potato___healthy", 2)

        r = self.client.get(reverse("parcelas"))

        fila = next(p for p in r.data if p["id"] == str(self.parcela.id))
        self.assertEqual(fila["total_diagnosticos"], 2)

    def test_agricultor_no_puede_consultar_el_historial(self):
        self.client.force_authenticate(self.agricultor)
        r = self.client.get(self._url(self.parcela))
        self.assertEqual(r.status_code, 403)

    def test_agricultor_no_puede_listar_parcelas(self):
        self.client.force_authenticate(self.agricultor)
        r = self.client.get(reverse("parcelas"))
        self.assertEqual(r.status_code, 403)

    def test_superusuario_puede_consultar(self):
        admin = Usuario.objects.create_superuser(username="root", password="x")
        self.client.force_authenticate(admin)
        r = self.client.get(self._url(self.parcela))
        self.assertEqual(r.status_code, 200)