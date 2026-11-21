from datetime import datetime, timedelta, timezone as tz
from types import SimpleNamespace

from django.test import SimpleTestCase

from .urgencia import ALTA, MEDIA, SIN_ALERTA, agrupar_por_urgencia, calcular_urgencia

AHORA = datetime(2026, 11, 10, 12, 0, tzinfo=tz.utc)
TARDIO = "Potato___Late_blight"
TEMPRANO = "Potato___Early_blight"
SANA = "Potato___healthy"


def diag(plaga, dias, severidad=""):
    return SimpleNamespace(
        plaga_detectada=plaga,
        plaga_nombre=plaga,
        nivel_severidad=severidad,
        fecha_captura=AHORA - timedelta(days=dias),
    )


class CalcularUrgenciaTests(SimpleTestCase):
    def test_sin_diagnosticos_no_hay_alerta(self):
        nivel, _ = calcular_urgencia([], AHORA)
        self.assertEqual(nivel, SIN_ALERTA)

    def test_ultimo_alta_es_urgencia_alta_aunque_haya_sanos_antes(self):
        nivel, _ = calcular_urgencia([diag(TARDIO, 1, "ALTA"), diag(SANA, 10, "NINGUNA")], AHORA)
        self.assertEqual(nivel, ALTA)

    def test_ultimo_media_es_urgencia_media(self):
        nivel, _ = calcular_urgencia([diag(TEMPRANO, 1, "MEDIA")], AHORA)
        self.assertEqual(nivel, MEDIA)

    def test_ultimo_sano_sin_medias_recientes_no_hay_alerta(self):
        nivel, _ = calcular_urgencia([diag(SANA, 1, "NINGUNA"), diag(TEMPRANO, 30, "MEDIA")], AHORA)
        self.assertEqual(nivel, SIN_ALERTA)

    def test_dos_medias_en_15_dias_con_ultimo_sano_es_media(self):
        diagnosticos = [diag(SANA, 1, "NINGUNA"), diag(TEMPRANO, 3, "MEDIA"), diag(TEMPRANO, 6, "MEDIA")]
        nivel, motivo = calcular_urgencia(diagnosticos, AHORA)
        self.assertEqual(nivel, MEDIA)
        self.assertIn("2 diagnósticos", motivo)

    def test_una_media_dentro_y_otra_fuera_de_la_ventana_no_alcanza(self):
        diagnosticos = [diag(SANA, 1, "NINGUNA"), diag(TEMPRANO, 3, "MEDIA"), diag(TEMPRANO, 20, "MEDIA")]
        nivel, _ = calcular_urgencia(diagnosticos, AHORA)
        self.assertEqual(nivel, SIN_ALERTA)

    def test_severidad_vacia_se_deduce_de_la_plaga(self):
        nivel, _ = calcular_urgencia([diag(TARDIO, 1, "")], AHORA)
        self.assertEqual(nivel, ALTA)


class AgruparPorUrgenciaTests(SimpleTestCase):
    def test_reparte_las_parcelas_en_su_grupo(self):
        parcelas = [
            SimpleNamespace(nombre="A", diagnosticos_ordenados=[diag(TARDIO, 1, "ALTA")]),
            SimpleNamespace(nombre="B", diagnosticos_ordenados=[diag(TEMPRANO, 1, "MEDIA")]),
            SimpleNamespace(nombre="C", diagnosticos_ordenados=[diag(SANA, 1, "NINGUNA")]),
            SimpleNamespace(nombre="D", diagnosticos_ordenados=[]),
        ]
        grupos = agrupar_por_urgencia(parcelas, AHORA)
        self.assertEqual([i["parcela"].nombre for i in grupos[ALTA]], ["A"])
        self.assertEqual([i["parcela"].nombre for i in grupos[MEDIA]], ["B"])
        self.assertEqual(sorted(i["parcela"].nombre for i in grupos[SIN_ALERTA]), ["C", "D"])