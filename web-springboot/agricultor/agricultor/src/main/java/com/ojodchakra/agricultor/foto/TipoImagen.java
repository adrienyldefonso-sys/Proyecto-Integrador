package com.ojodchakra.agricultor.foto;

import com.ojodchakra.agricultor.error.SolicitudInvalidaException;

/** Reconoce JPEG y PNG por sus primeros bytes, sin fiarse del nombre ni del Content-Type. */
public final class TipoImagen {

    private TipoImagen() {
    }

    public static String extensionDe(byte[] contenido) {
        if (contenido != null && contenido.length >= 3
                && (contenido[0] & 0xFF) == 0xFF
                && (contenido[1] & 0xFF) == 0xD8
                && (contenido[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (contenido != null && contenido.length >= 8
                && (contenido[0] & 0xFF) == 0x89
                && contenido[1] == 'P'
                && contenido[2] == 'N'
                && contenido[3] == 'G'
                && contenido[4] == 0x0D
                && contenido[5] == 0x0A
                && contenido[6] == 0x1A
                && contenido[7] == 0x0A) {
            return "png";
        }
        throw new SolicitudInvalidaException("La foto debe ser una imagen JPEG o PNG.");
    }
}