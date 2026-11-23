package com.ojodchakra.agricultor.diagnostico;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/diagnosticos")
public class DiagnosticoController {

    private final DiagnosticoServicio servicio;

    public DiagnosticoController(DiagnosticoServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiagnosticoRespuesta> registrar(
            @RequestPart("datos") @Valid DiagnosticoSolicitud datos,
            @RequestPart("foto") MultipartFile foto,
            HttpServletRequest request) {
        DiagnosticoServicio.Resultado resultado = servicio.registrar(datos, foto, request.getRemoteAddr());
        HttpStatus estado = resultado.creado() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(estado).body(DiagnosticoRespuesta.desde(resultado.diagnostico()));
    }
}