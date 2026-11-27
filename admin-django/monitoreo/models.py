
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

    id = models.CharField(
        max_length=100,
        primary_key=True,
        default=uuid.uuid4,
        editable=False
    )
    telefono = models.CharField(max_length=20, blank=True, null=True)
    rol = models.CharField(
        max_length=20,
        choices=Rol.choices,
        default=Rol.AGRICULTOR
    )

    def __str__(self):
        return self.username


class Parcela(models.Model):
    id = models.CharField(
        max_length=100,
        primary_key=True,
        default=uuid.uuid4,
        editable=False
    )
    usuario = models.ForeignKey(
        Usuario,
        on_delete=models.CASCADE,
        related_name="parcelas"
    )
    nombre = models.CharField(max_length=150)
    cultivo = models.CharField(max_length=100, blank=True, null=True)
    referencia = models.CharField(max_length=255, blank=True, null=True)
    latitud = models.FloatField(null=True, blank=True)
    longitud = models.FloatField(null=True, blank=True)
    fecha_creacion = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.nombre} ({self.usuario.username})"


class Diagnostico(models.Model):
    id = models.CharField(
        max_length=100,
        primary_key=True,
        default=uuid.uuid4,
        editable=False
    )
    usuario = models.ForeignKey(
        Usuario,
        on_delete=models.CASCADE,
        related_name="diagnosticos"
    )
    parcela = models.ForeignKey(
        Parcela,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="diagnosticos"
    )
    foto = models.ImageField(
        upload_to="diagnosticos/",
        null=True,
        blank=True
    )
    plaga_detectada = models.CharField(max_length=150)
    confianza = models.FloatField()
    nivel_severidad = models.CharField(
        max_length=50,
        blank=True,
        default=""
    )
    recomendacion = models.TextField(blank=True, default="")
    fecha_captura = models.DateTimeField(default=timezone.now)

    class EstadoRevision(models.TextChoices):
        NINGUNA = "NINGUNA", "Sin revisión"
        SOLICITADA = "SOLICITADA", "Revisión solicitada"
        CONFIRMADA = "CONFIRMADA", "Confirmado por agrónomo"
        CORREGIDA = "CORREGIDA", "Corregido por agrónomo"

    # El agricultor solo puede pedir revisión. Confirmar o corregir lo hace el agrónomo.
    estado_revision = models.CharField(
        max_length=20,
        choices=EstadoRevision.choices,
        default=EstadoRevision.NINGUNA,
    )
    etiqueta_validada = models.CharField(max_length=150, blank=True, default="")
    observacion_agronomo = models.TextField(blank=True, default="")
    fecha_revision = models.DateTimeField(null=True, blank=True)
    revisado_por = models.ForeignKey(
        Usuario,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="revisiones",
    )

    @property
    def plaga_nombre(self):
        return MAPA_PLAGAS.get(
            self.plaga_detectada,
            self.plaga_detectada
        )

    def __str__(self):
        return (
            f"{self.plaga_detectada} - "
            f"{self.fecha_captura.strftime('%Y-%m-%d %H:%M')}"
        )


class EventoAuditoria(models.Model):
    fecha = models.DateTimeField(default=timezone.now)
    accion = models.CharField(max_length=50)
    entidad = models.CharField(max_length=50)
    entidad_id = models.CharField(max_length=100)
    usuario_id = models.CharField(
        max_length=100,
        blank=True,
        default=""
    )
    origen = models.CharField(
        max_length=30,
        default="springboot"
    )
    ip = models.CharField(
        max_length=45,
        blank=True,
        default=""
    )
    detalle = models.TextField(
        blank=True,
        default=""
    )

    class Meta:
        ordering = ["-fecha"]
        indexes = [
            models.Index(fields=["entidad", "entidad_id"])
        ]

    def __str__(self):
        return f"{self.accion} {self.entidad} {self.entidad_id}"


class SesionMovil(models.Model):
    """Sesión de la app móvil. Solo se guarda el hash del token, nunca el token."""

    id = models.CharField(
        max_length=100,
        primary_key=True,
        default=uuid.uuid4,
        editable=False
    )
    usuario = models.ForeignKey(
        Usuario,
        on_delete=models.CASCADE,
        related_name="sesiones_movil"
    )
    token_hash = models.CharField(max_length=64, unique=True)
    creada = models.DateTimeField(auto_now_add=True)
    expira = models.DateTimeField()

    def __str__(self):
        return f"Sesión móvil de {self.usuario.username}"


class InicioSesionMovil(models.Model):
    """Cada vez que una cuenta entra desde la app móvil. Sirve para revisar quién usa la app y cuándo."""

    id = models.CharField(
        max_length=100,
        primary_key=True,
        default=uuid.uuid4,
        editable=False
    )
    usuario = models.ForeignKey(
        Usuario,
        on_delete=models.CASCADE,
        related_name="inicios_sesion"
    )
    fecha = models.DateTimeField(default=timezone.now)

    class Meta:
        ordering = ["-fecha"]

    def __str__(self):
        return f"Inicio de {self.usuario.username} el {self.fecha:%d/%m/%Y %H:%M}"
