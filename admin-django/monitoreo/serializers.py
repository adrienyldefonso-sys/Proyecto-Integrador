from rest_framework import serializers

from .models import MAPA_PLAGAS, Diagnostico, Parcela


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
            "estado_revision",
            "etiqueta_validada",
            "etiqueta_validada_nombre",
            "observacion_agronomo",
            "fecha_revision",
        ]

    etiqueta_validada_nombre = serializers.SerializerMethodField()

    def get_etiqueta_validada_nombre(self, obj):
        from .models import MAPA_PLAGAS
        return MAPA_PLAGAS.get(obj.etiqueta_validada, obj.etiqueta_validada) or None


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


class RevisionSerializer(serializers.ModelSerializer):
    plaga_nombre = serializers.CharField(read_only=True)
    foto_url = serializers.ImageField(source="foto", use_url=True, read_only=True)
    parcela_nombre = serializers.SerializerMethodField()
    agricultor = serializers.SerializerMethodField()
    etiqueta_validada_nombre = serializers.SerializerMethodField()

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
            "parcela_nombre",
            "agricultor",
            "estado_revision",
            "etiqueta_validada",
            "etiqueta_validada_nombre",
            "observacion_agronomo",
            "fecha_revision",
        ]

    def get_parcela_nombre(self, obj):
        return obj.parcela.nombre if obj.parcela else None

    def get_agricultor(self, obj):
        return obj.usuario.get_full_name() or obj.usuario.username

    def get_etiqueta_validada_nombre(self, obj):
        return MAPA_PLAGAS.get(obj.etiqueta_validada, obj.etiqueta_validada) or None
