import uuid

from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
        ('monitoreo', '0004_revision_agronomo'),
    ]

    operations = [
        migrations.CreateModel(
            name='SesionMovil',
            fields=[
                ('id', models.CharField(default=uuid.uuid4, editable=False, max_length=100, primary_key=True, serialize=False)),
                ('token_hash', models.CharField(max_length=64, unique=True)),
                ('creada', models.DateTimeField(auto_now_add=True)),
                ('expira', models.DateTimeField()),
                ('usuario', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='sesiones_movil', to=settings.AUTH_USER_MODEL)),
            ],
        ),
    ]
