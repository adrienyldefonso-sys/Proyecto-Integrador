from datetime import datetime, timedelta, timezone as tz
from types import SimpleNamespace

from django.test import SimpleTestCase

from .ruta import ordenar_y_numerar
from .urgencia import ALTA, MEDIA, SIN_ALERTA

AHORA = datetime(2026, 11, 10, 12, 0, tzinfo=tz.utc)


def item(nombre, nivel, dias=None):
    ultimo = (
        SimpleNamespace(fecha_captura=AHORA - timedelta(days=dias)) if dias is not None else None
    )
    return {"nivel": nivel, "motivo": "", "parcela": SimpleNamespace(nombre=nombre), "ultimo": ultimo}


def grupos_de(*items):
    grupos = {ALTA: [], MEDIA: [], SIN_ALERTA: []}
    for i in items:
        grupos[i["nivel"]].append(i)
    return grupos


class OrdenarRutaTests(SimpleTestCase):
    def test_dentro_de_cada_nivel_va_primero_el_diagnostico_mas_reciente(self):
        grupos = ordenar_y_numerar(grupos_de(item("vieja", ALTA, 9), item("nueva", ALTA, 1)))
        self.assertEqual([i["parcela"].nombre for i in grupos[ALTA]], ["nueva", "vieja"])

    def test_la_ruta_se_numera_primero_alta_y_luego_media(self):
        grupos = ordenar_y_numerar(grupos_de(item("m", MEDIA, 1), item("a", ALTA, 5)))
        self.assertEqual(grupos[ALTA][0]["orden"], 1)
        self.assertEqual(grupos[MEDIA][0]["orden"], 2)

    def test_sin_alerta_no_entra_en_la_ruta(self):
        grupos = ordenar_y_numerar(grupos_de(item("s", SIN_ALERTA, 1)))
        self.assertIsNone(grupos[SIN_ALERTA][0]["orden"])

    def test_las_parcelas_sin_diagnosticos_quedan_al_final_del_grupo(self):
        grupos = ordenar_y_numerar(
            grupos_de(item("vacia", SIN_ALERTA), item("sana", SIN_ALERTA, 2))
        )
        self.assertEqual([i["parcela"].nombre for i in grupos[SIN_ALERTA]], ["sana", "vacia"])