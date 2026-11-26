package com.ojodchakra.agricultor.usuario;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashContrasenaTest {

    // Generado con el mismo cálculo que Django (django.contrib.auth.hashers.PBKDF2PasswordHasher)
    private static final String HASH_DJANGO = "pbkdf2_sha256$870000$Ab3dEf9hIj1kLm2nOp4qRs$MpAJPn5x8I/Yv/gf3NZCeWcoXTpLoKlxT7XEzinQeA4=";

    @Test
    void aceptaUnHashCreadoPorDjango() {
        assertTrue(HashContrasena.verificar("clave-prueba-123", HASH_DJANGO));
        assertFalse(HashContrasena.verificar("otra-clave", HASH_DJANGO));
    }

    @Test
    void generaUnHashQueSeVerifica() {
        String hash = HashContrasena.generar("mi-clave-nueva-1");
        assertTrue(hash.startsWith("pbkdf2_sha256$"));
        assertTrue(HashContrasena.verificar("mi-clave-nueva-1", hash));
        assertFalse(HashContrasena.verificar("x", hash));
    }

    @Test
    void rechazaFormatosInvalidos() {
        assertFalse(HashContrasena.verificar("clave", null));
        assertFalse(HashContrasena.verificar("clave", "texto-sin-formato"));
        assertFalse(HashContrasena.verificar("clave", "md5$1$sal$hash"));
    }
}
