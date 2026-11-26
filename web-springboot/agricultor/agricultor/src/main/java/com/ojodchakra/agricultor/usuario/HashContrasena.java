package com.ojodchakra.agricultor.usuario;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cifra contraseñas en el mismo formato que Django: pbkdf2_sha256$iteraciones$sal$hash.
 * Así un usuario creado desde el panel web también puede entrar desde el celular, y al revés.
 */
public final class HashContrasena {

    private static final String ALGORITMO = "pbkdf2_sha256";
    private static final String DERIVACION = "PBKDF2WithHmacSHA256";
    // Django 5.x usa 870 000 iteraciones. Al verificar se usa el número que trae cada hash.
    private static final int ITERACIONES = 870_000;
    private static final int LARGO_BITS = 256;
    private static final int LARGO_SAL = 22;
    private static final String CARACTERES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private HashContrasena() {
    }

    public static String generar(String contrasena) {
        String sal = generarSal();
        try {
            return ALGORITMO + "$" + ITERACIONES + "$" + sal + "$" + derivar(contrasena, sal, ITERACIONES);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("El servidor no puede cifrar contraseñas.", e);
        }
    }

    public static boolean verificar(String contrasena, String almacenada) {
        if (contrasena == null || almacenada == null) {
            return false;
        }
        String[] partes = almacenada.split("\\$");
        if (partes.length != 4 || !ALGORITMO.equals(partes[0])) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            String calculado = derivar(contrasena, partes[2], iteraciones);
            return MessageDigest.isEqual(
                    calculado.getBytes(StandardCharsets.UTF_8),
                    partes[3].getBytes(StandardCharsets.UTF_8));
        } catch (NumberFormatException | GeneralSecurityException e) {
            return false;
        }
    }

    private static String derivar(String contrasena, String sal, int iteraciones)
            throws GeneralSecurityException {
        PBEKeySpec especificacion = new PBEKeySpec(
                contrasena.toCharArray(), sal.getBytes(StandardCharsets.UTF_8), iteraciones, LARGO_BITS);
        try {
            byte[] bytes = SecretKeyFactory.getInstance(DERIVACION).generateSecret(especificacion).getEncoded();
            return Base64.getEncoder().encodeToString(bytes);
        } finally {
            especificacion.clearPassword();
        }
    }

    private static String generarSal() {
        StringBuilder sal = new StringBuilder(LARGO_SAL);
        for (int i = 0; i < LARGO_SAL; i++) {
            sal.append(CARACTERES.charAt(ALEATORIO.nextInt(CARACTERES.length())));
        }
        return sal.toString();
    }
}
