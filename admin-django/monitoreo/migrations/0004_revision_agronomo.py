from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
        ('monitoreo', '0003_eventoauditoria'),
    ]

    operations = [
        migrations.AddField(
            model_name='diagnostico',
            name='estado_revision',
            field=models.CharField(
                choices=[
                    ('NINGUNA', 'Sin revisión'),
                    ('SOLICITADA', 'Revisión solicitada'),
                    ('CONFIRMADA', 'Confirmado por agrónomo'),
                    ('CORREGIDA', 'Corregido por agrónomo'),
                ],
                default='NINGUNA',
                max_length=20,
            ),
        ),
        migrations.AddField(
            model_name='diagnostico',
            name='etiqueta_validada',
            field=models.CharField(blank=True, default='', max_length=150),
        ),
        migrations.AddField(
            model_name='diagnostico',
            name='observacion_agronomo',
            field=models.TextField(blank=True, default=''),
        ),
        migrations.AddField(
            model_name='diagnostico',
            name='fecha_revision',
            field=models.DateTimeField(blank=True, null=True),
        ),
        migrations.AddField(
            model_name='diagnostico',
            name='revisado_por',
            field=models.ForeignKey(
                blank=True,
                null=True,
                on_delete=django.db.models.deletion.SET_NULL,
                related_name='revisiones',
                to=settings.AUTH_USER_MODEL,
            ),
        ),
    ]
