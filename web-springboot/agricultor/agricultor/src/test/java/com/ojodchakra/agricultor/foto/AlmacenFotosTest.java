package com.ojodchakra.agricultor.foto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlmacenFotosTest {

    @TempDir
    Path carpeta;

    @Test
    void guardaLaFotoEnLaSubcarpetaDiagnosticos() throws Exception {
        AlmacenFotos almacen = new AlmacenFotos(carpeta.toString());

        almacen.guardar(AlmacenFotos.rutaRelativa("abc", "jpg"), new byte[]{1, 2, 3});

        assertArrayEquals(new byte[]{1, 2, 3},
                Files.readAllBytes(carpeta.resolve("diagnosticos").resolve("abc.jpg")));
    }

    @Test
    void laRutaRelativaUsaElIdYLaExtension() {
        assertEquals("diagnosticos/abc.png", AlmacenFotos.rutaRelativa("abc", "png"));
    }

    @Test
    void rechazaRutasQueSalenDeLaCarpetaMedia() {
        AlmacenFotos almacen = new AlmacenFotos(carpeta.toString());

        assertThrows(IllegalArgumentException.class,
                () -> almacen.guardar("../fuera.jpg", new byte[]{1}));
    }
}