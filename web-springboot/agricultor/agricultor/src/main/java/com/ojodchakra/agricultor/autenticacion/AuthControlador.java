package com.ojodchakra.agricultor.autenticacion;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthControlador {

    private final AuthServicio servicio;

    public AuthControlador(AuthServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/registro")
    public ResponseEntity<RespuestaAuth> registrar(@Valid @RequestBody SolicitudRegistro solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrar(solicitud));
    }

    @PostMapping("/login")
    public RespuestaAuth login(@Valid @RequestBody SolicitudLogin solicitud) {
        return servicio.login(solicitud);
    }

    @PostMapping("/salir")
    public ResponseEntity<Void> salir(@RequestHeader(value = "Authorization", required = false) String autorizacion) {
        servicio.cerrarSesion(AuthServicio.extraerToken(autorizacion));
        return ResponseEntity.noContent().build();
    }
}
