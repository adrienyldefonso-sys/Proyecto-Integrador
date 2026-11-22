package com.ojodchakra.agricultor.parcela;

import java.time.Instant;
import java.time.ZoneOffset;

/** Lo que ve el cliente. Django guarda las fechas en UTC, por eso se interpreta así. */
public record ParcelaRespuesta(
        String id,
        String usuarioId,
        String nombre,
        String cultivo,
        String referencia,
        Double latitud,
        Double longitud,
        Instant fechaCreacion) {

    public static ParcelaRespuesta desde(Parcela p) {
        return new ParcelaRespuesta(
                p.getId(),
                p.getUsuarioId(),
                p.getNombre(),
                p.getCultivo(),
                p.getReferencia(),
                p.getLatitud(),
                p.getLongitud(),
                p.getFechaCreacion().toInstant(ZoneOffset.UTC));
    }
}