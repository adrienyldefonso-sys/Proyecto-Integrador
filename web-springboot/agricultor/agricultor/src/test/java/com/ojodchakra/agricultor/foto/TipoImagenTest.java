package com.ojodchakra.agricultor.foto;

import com.ojodchakra.agricultor.error.SolicitudInvalidaException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TipoImagenTest {

    @Test
    void unJpegSeReconoce() {
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16};
        assertEquals("jpg", TipoImagen.extensionDe(jpeg));
    }

    @Test
    void unPngSeReconoce() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0};
        assertEquals("png", TipoImagen.extensionDe(png));
    }

    @Test
    void unTextoNoEsUnaImagenAunqueSeLlameFotoJpg() {
        byte[] texto = "esto no es una imagen".getBytes(StandardCharsets.UTF_8);
        assertThrows(SolicitudInvalidaException.class, () -> TipoImagen.extensionDe(texto));
    }

    @Test
    void unArchivoVacioNoEsUnaImagen() {
        assertThrows(SolicitudInvalidaException.class, () -> TipoImagen.extensionDe(new byte[0]));
    }
}