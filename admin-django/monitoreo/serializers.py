from rest_framework import serializers

from .models import Diagnostico, Parcela


class DiagnosticoSerializer(serializers.ModelSerializer):
    plaga_nombre = serializers.CharField(read_only=True)
    foto_url = serializers.ImageField(source="foto", use_url=True, read_only=True)

    class Meta:
        model = Diagnostico
        fields = [
            "id",
            "fecha_captura",
            "plaga_detectada",
            "plaga_nombre",
            "confianza",
            "nivel_severidad",
            "recomendacion",
            "foto_url",
        ]


class ParcelaSerializer(serializers.ModelSerializer):
    agricultor = serializers.SerializerMethodField()
    total_diagnosticos = serializers.SerializerMethodField()

    class Meta:
        model = Parcela
        fields = [
            "id",
            "nombre",
            "cultivo",
            "referencia",
            "latitud",
            "longitud",
            "agricultor",
            "total_diagnosticos",
        ]

    def get_agricultor(self, obj):
        return obj.usuario.get_full_name() or obj.usuario.username

    def get_total_diagnosticos(self, obj):
        total = getattr(obj, "total_diagnosticos", None)
        return total if total is not None else obj.diagnosticos.count()