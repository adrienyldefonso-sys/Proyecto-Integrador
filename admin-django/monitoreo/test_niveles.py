from django.test import SimpleTestCase

from .niveles import severidad_efectiva


class SeveridadEfectivaTests(SimpleTestCase):
    def test_usa_la_severidad_guardada(self):
        self.assertEqual(severidad_efectiva("Potato___healthy", "ALTA"), "ALTA")

    def test_acepta_minusculas_y_espacios(self):
        self.assertEqual(severidad_efectiva("Potato___healthy", " media "), "MEDIA")

    def test_si_esta_vacia_se_deduce_de_la_plaga(self):
        self.assertEqual(severidad_efectiva("Potato___Late_blight", ""), "ALTA")
        self.assertEqual(severidad_efectiva("Potato___Early_blight", None), "MEDIA")
        self.assertEqual(severidad_efectiva("Potato___healthy", ""), "NINGUNA")

    def test_plaga_desconocida_sin_severidad_no_genera_alerta(self):
        self.assertEqual(severidad_efectiva("Otra_plaga", ""), "NINGUNA")

    def test_un_valor_invalido_se_ignora(self):
        self.assertEqual(severidad_efectiva("Potato___Late_blight", "urgente"), "ALTA")