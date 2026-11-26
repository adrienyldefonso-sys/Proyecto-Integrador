package com.ojodchakra.agricultor.error;

public class NoAutorizadoException extends RuntimeException {

    public NoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
