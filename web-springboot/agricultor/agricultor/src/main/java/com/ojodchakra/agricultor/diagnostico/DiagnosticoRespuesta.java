package com.ojodchakra.agricultor.diagnostico;

import java.time.Instant;
import java.time.ZoneOffset;

public record DiagnosticoRespuesta(
        String id,
        String usuarioId,
        String parcelaId,
        String foto,
        String plagaDetectada,
        Double confianza,
        String nivelSeveridad,
        String recomendacion,
        Instant fechaCaptura) {

    public static DiagnosticoRespuesta desde(Diagnostico d) {
        return new DiagnosticoRespuesta(
                d.getId(),
                d.getUsuarioId(),
                d.getParcelaId(),
                d.getFoto(),
                d.getPlagaDetectada(),
                d.getConfianza(),
                vacioANulo(d.getNivelSeveridad()),
                vacioANulo(d.getRecomendacion()),
                d.getFechaCaptura().toInstant(ZoneOffset.UTC));
    }

    private static String vacioANulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto;
    }
}