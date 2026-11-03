import uuid
from django.db import models
from django.contrib.auth.models import AbstractUser
from django.utils import timezone

MAPA_PLAGAS = {
    "Potato___Late_blight": "Tizón tardío",
    "Potato___Early_blight": "Tizón temprano",
    "Potato___healthy": "Saludable",
}

class Usuario(AbstractUser):
    class Rol(models.TextChoices):
        AGRONOMO = "AGRONOMO", "Agrónomo"
        AGRICULTOR = "AGRICULTOR", "Agricultor"

    id = models.CharField(max_length=100, primary_key=True, default=uuid.uuid4, editable=False)
    telefono = models.CharField(max_length=20, blank=True, null=True)
    rol = models.CharField(max_length=20, choices=Rol.choices, default=Rol.AGRICULTOR)

    def __str__(self):
        return self.username

class Parcela(models.Model):
    id = models.CharField(max_length=100, primary_key=True, default=uuid.uuid4, editable=False)
    usuario = models.ForeignKey(Usuario, on_delete=models.CASCADE, related_name="parcelas")
    nombre = models.CharField(max_length=150)
    cultivo = models.CharField(max_length=100, blank=True, null=True)
    referencia = models.CharField(max_length=255, blank=True, null=True)
    latitud = models.FloatField(null=True, blank=True)
    longitud = models.FloatField(null=True, blank=True)
    fecha_creacion = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.nombre} ({self.usuario.username})"

class Diagnostico(models.Model):
    id = models.CharField(max_length=100, primary_key=True, default=uuid.uuid4, editable=False)
    usuario = models.ForeignKey(Usuario, on_delete=models.CASCADE, related_name="diagnosticos")
    parcela = models.ForeignKey(Parcela, on_delete=models.SET_NULL, null=True, blank=True, related_name="diagnosticos")
    foto = models.ImageField(upload_to="diagnosticos/", null=True, blank=True)
    plaga_detectada = models.CharField(max_length=150)
    confianza = models.FloatField()
    nivel_severidad = models.CharField(max_length=50, blank=True, default="")
    recomendacion = models.TextField(blank=True, default="")
    fecha_captura = models.DateTimeField(default=timezone.now)

    @property
    def plaga_nombre(self):
        return MAPA_PLAGAS.get(self.plaga_detectada, self.plaga_detectada)

    def __str__(self):
        return f"{self.plaga_detectada} - {self.fecha_captura.strftime('%Y-%m-%d %H:%M')}"