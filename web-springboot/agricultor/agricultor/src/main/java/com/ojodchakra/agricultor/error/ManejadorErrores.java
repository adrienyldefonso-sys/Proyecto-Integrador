package com.ojodchakra.agricultor.error;

import com.ojodchakra.agricultor.parcela.ParcelaNoEncontradaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Una sola forma de responder los errores, para que la app móvil los entienda siempre igual. */
@RestControllerAdvice
public class ManejadorErrores extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(ParcelaNoEncontradaException.class)
    public ProblemDetail parcelaNoEncontrada(ParcelaNoEncontradaException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problema.setTitle("Parcela no encontrada");
        return problema;
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ProblemDetail solicitudInvalida(SolicitudInvalidaException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problema.setTitle("Solicitud inválida");
        return problema;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail conflicto(DataIntegrityViolationException e) {
        // La app solo recibe un mensaje general; la causa exacta queda en la consola de Spring Boot
        log.warn("Conflicto al guardar desde la app: {}", e.getMostSpecificCause().getMessage());
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "No se pudo guardar por un conflicto con datos existentes.");
        problema.setTitle("Conflicto");
        return problema;
    }

    @ExceptionHandler(NoAutorizadoException.class)
    public ProblemDetail noAutorizado(NoAutorizadoException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
        problema.setTitle("No autorizado");
        return problema;
    }

    @ExceptionHandler(ConflictoRegistroException.class)
    public ProblemDetail registroDuplicado(ConflictoRegistroException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problema.setTitle("Registro duplicado");
        return problema;
    }

    /** Cualquier error no previsto: se guarda la causa completa en la consola y la app recibe un mensaje claro. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail errorInterno(Exception e) {
        log.error("Error no controlado al atender una solicitud de la app", e);
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "El servidor tuvo un problema. Revisa la consola de Spring Boot para ver el detalle.");
        problema.setTitle("Error interno");
        return problema;
    }

    @ExceptionHandler(UncheckedIOException.class)
    public ProblemDetail errorDeArchivo(UncheckedIOException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar la foto en el servidor.");
        problema.setTitle("Error al guardar la foto");
        return problema;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud.");
        problema.setTitle("Datos inválidos");
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));
        problema.setProperty("errores", errores);
        return ResponseEntity.badRequest().body(problema);
    }
}