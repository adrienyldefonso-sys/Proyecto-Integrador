import uuid

from django.contrib.auth.models import AbstractUser
from django.db import models
from django.utils import timezone

NOMBRES_PLAGA = {
    "Potato___Early_blight": "Tizón temprano",
    "Potato___Late_blight": "Tizón tardío",
    "Potato___healthy": "Planta sana",
}


class Usuario(AbstractUser):
    class Rol(models.TextChoices):
        AGRICULTOR = "AGRICULTOR", "Agricultor"
        AGRONOMO = "AGRONOMO", "Agrónomo"

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    celular = models.CharField(max_length=20, blank=True)
    rol = models.CharField(max_length=20, choices=Rol.choices, default=Rol.AGRICULTOR)

    class Meta:
        db_table = "usuarios"


class Parcela(models.Model):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    usuario = models.ForeignKey(Usuario, on_delete=models.CASCADE, related_name="parcelas")
    nombre = models.CharField(max_length=100)
    cultivo = models.CharField(max_length=50)
    referencia = models.CharField(max_length=150, blank=True)
    latitud = models.DecimalField(max_digits=10, decimal_places=7)
    longitud = models.DecimalField(max_digits=10, decimal_places=7)
    fecha_registro = models.DateTimeField(default=timezone.now)

    class Meta:
        db_table = "parcelas"

    def __str__(self):
        return f"{self.nombre} ({self.cultivo})"


class Diagnostico(models.Model):
    class Severidad(models.TextChoices):
        NINGUNA = "NINGUNA", "Ninguna"
        BAJA = "BAJA", "Baja"
        MEDIA = "MEDIA", "Media"
        ALTA = "ALTA", "Alta"

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    usuario = models.ForeignKey(Usuario, on_delete=models.CASCADE, related_name="diagnosticos")
    parcela = models.ForeignKey(
        Parcela, null=True, blank=True, on_delete=models.SET_NULL, related_name="diagnosticos"
    )
    foto = models.ImageField(upload_to="diagnosticos/%Y/%m/")
    plaga_detectada = models.CharField(max_length=60)
    confianza = models.FloatField()
    nivel_severidad = models.CharField(max_length=10, choices=Severidad.choices, blank=True)
    recomendacion = models.TextField(blank=True)
    fecha_captura = models.DateTimeField()

    class Meta:
        db_table = "diagnosticos"
        ordering = ["-fecha_captura"]
        indexes = [models.Index(fields=["parcela", "-fecha_captura"])]

    @property
    def plaga_nombre(self):
        return NOMBRES_PLAGA.get(self.plaga_detectada, self.plaga_detectada)

    def __str__(self):
        return f"{self.plaga_nombre} - {self.fecha_captura:%d/%m/%Y}"