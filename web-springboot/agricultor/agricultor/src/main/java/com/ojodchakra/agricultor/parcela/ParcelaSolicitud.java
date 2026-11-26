package com.ojodchakra.agricultor.parcela;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record ParcelaSolicitud(
        @NotBlank(message = "El id es obligatorio")
        @Pattern(regexp = "^[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}$",
                message = "El id debe ser un UUID")
        String id,

        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String usuarioId,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "Máximo 150 caracteres")
        String nombre,

        @NotBlank(message = "El cultivo es obligatorio")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String cultivo,

        @Size(max = 255, message = "Máximo 255 caracteres")
        String referencia,

        // Opcionales: la parcela puede llegar sin ubicación. Deben venir ambas o ninguna (ver ParcelaServicio)
        @DecimalMin(value = "-90.0", message = "La latitud va de -90 a 90")
        @DecimalMax(value = "90.0", message = "La latitud va de -90 a 90")
        Double latitud,

        @DecimalMin(value = "-180.0", message = "La longitud va de -180 a 180")
        @DecimalMax(value = "180.0", message = "La longitud va de -180 a 180")
        Double longitud,

        @NotNull(message = "La fecha de registro es obligatoria")
        Instant fechaRegistro) {
}