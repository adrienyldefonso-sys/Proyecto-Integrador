package com.ojodchakra.agricultor.diagnostico;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record DiagnosticoSolicitud(
        @NotBlank(message = "El id es obligatorio")
        @Pattern(regexp = "^[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}$",
                message = "El id debe ser un UUID")
        String id,

        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String usuarioId,

        @Pattern(regexp = "^$|^[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}$",
                message = "La parcela debe ser un UUID")
        String parcelaId,

        @NotBlank(message = "La plaga detectada es obligatoria")
        @Size(max = 150, message = "Máximo 150 caracteres")
        String plagaDetectada,

        @NotNull(message = "La confianza es obligatoria")
        @DecimalMin(value = "0.0", message = "La confianza va de 0 a 100")
        @DecimalMax(value = "100.0", message = "La confianza va de 0 a 100")
        Double confianza,

        @Pattern(regexp = "^(ALTA|MEDIA|BAJA|NINGUNA)?$",
                message = "La severidad debe ser ALTA, MEDIA, BAJA o NINGUNA")
        String nivelSeveridad,

        @Size(max = 2000, message = "Máximo 2000 caracteres")
        String recomendacion,

        @NotNull(message = "La fecha de captura es obligatoria")
        Instant fechaCaptura) {
}