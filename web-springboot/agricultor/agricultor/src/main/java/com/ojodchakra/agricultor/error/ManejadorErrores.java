package com.ojodchakra.agricultor.error;

import com.ojodchakra.agricultor.parcela.ParcelaNoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Una sola forma de responder los errores, para que la app móvil los entienda siempre igual. */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(ParcelaNoEncontradaException.class)
    public ProblemDetail parcelaNoEncontrada(ParcelaNoEncontradaException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problema.setTitle("Parcela no encontrada");
        return problema;
    }
}