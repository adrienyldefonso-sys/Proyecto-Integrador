package com.ojodchakra.agricultor.autenticacion;

import jakarta.validation.constraints.NotBlank;

public record SolicitudLogin(
        @NotBlank String usuario,
        @NotBlank String contrasena
) {
}
