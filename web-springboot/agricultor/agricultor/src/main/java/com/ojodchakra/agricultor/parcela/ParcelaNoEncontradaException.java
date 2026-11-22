package com.ojodchakra.agricultor.parcela;

public class ParcelaNoEncontradaException extends RuntimeException {

    public ParcelaNoEncontradaException(String id) {
        super("No existe una parcela con el id " + id);
    }
}