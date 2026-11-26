package com.ojodchakra.agricultor.autenticacion;

import java.time.Instant;

public record RespuestaAuth(
        String usuarioId,
        String usuario,
        String nombre,
        String rol,
        String token,
        Instant expira
) {
}
