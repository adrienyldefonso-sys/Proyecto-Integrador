import random
from datetime import timedelta
from decimal import Decimal
from io import BytesIO

from django.core.files.base import ContentFile
from django.core.management.base import BaseCommand
from django.utils import timezone
from PIL import Image, ImageDraw

from monitoreo.models import Diagnostico, Parcela, Usuario

CASOS = {
    "Potato___Late_blight": (
        "ALTA",
        "Actúa de inmediato: retira las plantas muy afectadas y consulta a un ingeniero agrónomo.",
        (120, 70, 50),
    ),
    "Potato___Early_blight": (
        "MEDIA",
        "Retira las hojas afectadas y consulta qué fungicida autorizado aplicar.",
        (150, 120, 60),
    ),
    "Potato___healthy": (
        "NINGUNA",
        "No necesitas aplicar ningún tratamiento por ahora.",
        (60, 140, 70),
    ),
}

PARCELAS = [
    ("Finca Sol Naciente", "Papa", "Chosica", "-11.9333000", "-76.7000000"),
    ("Chacra Los Andes", "Papa", "Huarochirí", "-12.1300000", "-76.2300000"),
]


def _imagen(plaga):
    color = CASOS[plaga][2]
    imagen = Image.new("RGB", (480, 360), color)
    ImageDraw.Draw(imagen).text((20, 20), f"DEMO: {plaga}", fill=(255, 255, 255))
    salida = BytesIO()
    imagen.save(salida, format="JPEG")
    return ContentFile(salida.getvalue())


class Command(BaseCommand):
    help = "Crea usuarios, parcelas y diagnósticos de demostración (solo para desarrollo)."

    def handle(self, *args, **options):
        rng = random.Random(7)

        agronomo, creado = Usuario.objects.get_or_create(
            username="agronomo",
            defaults={
                "first_name": "Ana",
                "last_name": "Torres",
                "rol": Usuario.Rol.AGRONOMO,
                "is_staff": True,
                "is_superuser": True,
            },
        )
        if creado:
            agronomo.set_password("agronomo123")
            agronomo.save()

        agricultor, creado = Usuario.objects.get_or_create(
            username="agricultor1",
            defaults={
                "first_name": "Luis",
                "last_name": "Quispe",
                "celular": "999888777",
                "rol": Usuario.Rol.AGRICULTOR,
            },
        )
        if creado:
            agricultor.set_password("agricultor123")
            agricultor.save()

        for nombre, cultivo, referencia, lat, lon in PARCELAS:
            parcela, _ = Parcela.objects.get_or_create(
                usuario=agricultor,
                nombre=nombre,
                defaults={
                    "cultivo": cultivo,
                    "referencia": referencia,
                    "latitud": Decimal(lat),
                    "longitud": Decimal(lon),
                },
            )
            if parcela.diagnosticos.exists():
                continue

            for _ in range(6):
                plaga = rng.choice(list(CASOS))
                severidad, recomendacion, _color = CASOS[plaga]
                diagnostico = Diagnostico(
                    usuario=agricultor,
                    parcela=parcela,
                    plaga_detectada=plaga,
                    confianza=round(rng.uniform(70, 99.9), 2),
                    nivel_severidad=severidad,
                    recomendacion=recomendacion,
                    fecha_captura=timezone.now() - timedelta(days=rng.randint(1, 60)),
                )
                diagnostico.foto.save(f"demo_{diagnostico.id}.jpg", _imagen(plaga), save=False)
                diagnostico.save()

        self.stdout.write(self.style.SUCCESS("Datos de demostración cargados."))
        self.stdout.write("Agrónomo: agronomo / agronomo123 (solo para desarrollo)")