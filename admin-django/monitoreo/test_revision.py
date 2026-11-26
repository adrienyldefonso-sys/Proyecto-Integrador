from django.urls import reverse
from django.utils import timezone
from rest_framework.test import APITestCase

from .models import Diagnostico, EventoAuditoria, Parcela, Usuario

E = Diagnostico.EstadoRevision
TIZON_TARDIO = "Potato___Late_blight"
TIZON_TEMPRANO = "Potato___Early_blight"


class RevisionAgronomoTests(APITestCase):
    def setUp(self):
        self.agronomo = Usuario.objects.create_user(
            username="agro", password="x", rol=Usuario.Rol.AGRONOMO
        )
        self.agricultor = Usuario.objects.create_user(username="agri", password="x")
        self.parcela = Parcela.objects.create(
            usuario=self.agricultor, nombre="Finca Norte", cultivo="Papa"
        )
        self.diagnostico = Diagnostico.objects.create(
            usuario=self.agricultor,
            parcela=self.parcela,
            plaga_detectada=TIZON_TARDIO,
            confianza=88.0,
            estado_revision=E.SOLICITADA,
            fecha_captura=timezone.now(),
        )
        self.client.force_authenticate(self.agronomo)

    def _revisar(self, datos):
        return self.client.post(
            reverse("revisar-diagnostico", kwargs={"diagnostico_id": self.diagnostico.id}),
            datos,
            format="json",
        )

    def test_la_cola_muestra_solo_las_revisiones_pendientes(self):
        Diagnostico.objects.create(
            usuario=self.agricultor, plaga_detectada=TIZON_TEMPRANO, confianza=70.0,
        )  # sin revisión: no debe aparecer

        respuesta = self.client.get(reverse("revisiones"))

        self.assertEqual(respuesta.status_code, 200)
        ids = [fila["id"] for fila in respuesta.data]
        self.assertEqual(ids, [self.diagnostico.id])
        self.assertEqual(respuesta.data[0]["parcela_nombre"], "Finca Norte")
        self.assertEqual(respuesta.data[0]["estado_revision"], "SOLICITADA")

    def test_un_filtro_desconocido_responde_400(self):
        respuesta = self.client.get(reverse("revisiones"), {"estado": "inventado"})
        self.assertEqual(respuesta.status_code, 400)

    def test_confirmar_deja_la_plaga_original_y_queda_en_auditoria(self):
        respuesta = self._revisar({"decision": "CONFIRMAR", "observacion": "Se ve bien."})

        self.assertEqual(respuesta.status_code, 200)
        self.diagnostico.refresh_from_db()
        self.assertEqual(self.diagnostico.estado_revision, E.CONFIRMADA)
        self.assertEqual(self.diagnostico.etiqueta_validada, TIZON_TARDIO)
        self.assertEqual(self.diagnostico.observacion_agronomo, "Se ve bien.")
        self.assertEqual(self.diagnostico.revisado_por, self.agronomo)
        self.assertTrue(
            EventoAuditoria.objects.filter(accion="REVISION_AGRONOMO", entidad_id=self.diagnostico.id).exists()
        )

    def test_corregir_guarda_la_plaga_validada(self):
        respuesta = self._revisar({
            "decision": "corregir",  # sin importar mayúsculas
            "etiqueta_validada": TIZON_TEMPRANO,
            "observacion": "Manchas concéntricas, es tizón temprano.",
        })

        self.assertEqual(respuesta.status_code, 200)
        self.diagnostico.refresh_from_db()
        self.assertEqual(self.diagnostico.estado_revision, E.CORREGIDA)
        self.assertEqual(self.diagnostico.etiqueta_validada, TIZON_TEMPRANO)
        self.assertEqual(respuesta.data["etiqueta_validada_nombre"], "Tizón temprano")
        # El resultado original del modelo no se borra
        self.assertEqual(self.diagnostico.plaga_detectada, TIZON_TARDIO)

    def test_corregir_a_la_misma_plaga_responde_400(self):
        respuesta = self._revisar({"decision": "CORREGIR", "etiqueta_validada": TIZON_TARDIO})
        self.assertEqual(respuesta.status_code, 400)

    def test_corregir_con_una_plaga_inexistente_responde_400(self):
        respuesta = self._revisar({"decision": "CORREGIR", "etiqueta_validada": "Gusano"})
        self.assertEqual(respuesta.status_code, 400)

    def test_una_decision_invalida_responde_400(self):
        self.assertEqual(self._revisar({"decision": "BORRAR"}).status_code, 400)

    def test_un_agricultor_no_puede_revisar(self):
        self.client.force_authenticate(self.agricultor)
        self.assertEqual(self._revisar({"decision": "CONFIRMAR"}).status_code, 403)
        self.diagnostico.refresh_from_db()
        self.assertEqual(self.diagnostico.estado_revision, E.SOLICITADA)

    def test_la_sesion_entrega_la_cookie_csrf_y_el_rol(self):
        respuesta = self.client.get(reverse("sesion"))
        self.assertEqual(respuesta.status_code, 200)
        self.assertTrue(respuesta.data["autenticado"])
        self.assertTrue(respuesta.data["es_agronomo"])
        self.assertIn("csrftoken", respuesta.cookies)
