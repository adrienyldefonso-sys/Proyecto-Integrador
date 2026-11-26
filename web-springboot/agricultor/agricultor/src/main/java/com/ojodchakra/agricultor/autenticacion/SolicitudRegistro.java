package com.ojodchakra.agricultor.autenticacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SolicitudRegistro(
        @NotBlank
        @Size(min = 3, max = 30)
        @Pattern(regexp = "^[A-Za-z0-9._]+$", message = "Usa solo letras, números, punto o guion bajo.")
        String usuario,

        @NotBlank
        @Size(min = 8, max = 128, message = "La contraseña debe tener al menos 8 caracteres.")
        String contrasena,

        @NotBlank
        @Size(max = 150)
        String nombre,

        @Size(max = 20)
        String telefono
) {
}
