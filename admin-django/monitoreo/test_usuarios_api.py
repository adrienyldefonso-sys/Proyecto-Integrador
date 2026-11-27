from datetime import timedelta

from django.utils import timezone
from rest_framework.test import APITestCase

from .models import InicioSesionMovil, Parcela, SesionMovil, Usuario


class GestionUsuariosTests(APITestCase):
    def setUp(self):
        self.agronomo = Usuario.objects.create(
            username="agronomo1", rol=Usuario.Rol.AGRONOMO, is_staff=True
        )
        self.agricultor = Usuario.objects.create(
            username="juan", first_name="Juan Pérez", telefono="912345678",
            rol=Usuario.Rol.AGRICULTOR,
        )
        self.ahora = timezone.now()
        InicioSesionMovil.objects.create(usuario=self.agricultor, fecha=self.ahora - timedelta(days=2))
        InicioSesionMovil.objects.create(usuario=self.agricultor, fecha=self.ahora - timedelta(hours=1))
        Parcela.objects.create(usuario=self.agricultor, nombre="Lote norte")
        SesionMovil.objects.create(
            usuario=self.agricultor, token_hash="a" * 64, expira=self.ahora + timedelta(days=5)
        )
        SesionMovil.objects.create(
            usuario=self.agricultor, token_hash="b" * 64, expira=self.ahora - timedelta(days=1)
        )
        self.client.force_authenticate(self.agronomo)

    def test_solo_el_agronomo_ve_la_lista(self):
        self.client.force_authenticate(self.agricultor)
        self.assertEqual(self.client.get("/api/usuarios/").status_code, 403)

    def test_lista_solo_agricultores_con_totales(self):
        respuesta = self.client.get("/api/usuarios/")
        self.assertEqual(respuesta.status_code, 200)
        usuarios = {u["usuario"]: u for u in respuesta.json()}
        self.assertNotIn("agronomo1", usuarios)
        juan = usuarios["juan"]
        self.assertEqual(juan["parcelas"], 1)
        self.assertEqual(juan["sesiones_vigentes"], 1)  # la sesión vencida no cuenta
        self.assertEqual(juan["telefono"], "912345678")

    def test_detalle_trae_inicios_y_parcelas(self):
        respuesta = self.client.get(f"/api/usuarios/{self.agricultor.id}/")
        self.assertEqual(respuesta.status_code, 200)
        datos = respuesta.json()
        self.assertEqual(len(datos["inicios"]), 2)
        self.assertEqual(datos["lista_parcelas"][0]["nombre"], "Lote norte")

    def test_desactivar_cierra_sus_sesiones(self):
        respuesta = self.client.post(
            f"/api/usuarios/{self.agricultor.id}/estado/", {"activo": False}, format="json"
        )
        self.assertEqual(respuesta.status_code, 200)
        self.assertFalse(respuesta.json()["activo"])
        self.agricultor.refresh_from_db()
        self.assertFalse(self.agricultor.is_active)
        self.assertFalse(SesionMovil.objects.filter(usuario=self.agricultor).exists())

    def test_no_se_puede_desactivar_al_agronomo(self):
        respuesta = self.client.post(
            f"/api/usuarios/{self.agronomo.id}/estado/", {"activo": False}, format="json"
        )
        self.assertEqual(respuesta.status_code, 404)

    def test_estado_exige_true_o_false(self):
        respuesta = self.client.post(
            f"/api/usuarios/{self.agricultor.id}/estado/", {"activo": "no"}, format="json"
        )
        self.assertEqual(respuesta.status_code, 400)

    def test_cerrar_sesiones_devuelve_cuantas_cerro(self):
        respuesta = self.client.post(f"/api/usuarios/{self.agricultor.id}/cerrar-sesiones/")
        self.assertEqual(respuesta.json(), {"cerradas": 2})
        self.assertFalse(SesionMovil.objects.filter(usuario=self.agricultor).exists())
