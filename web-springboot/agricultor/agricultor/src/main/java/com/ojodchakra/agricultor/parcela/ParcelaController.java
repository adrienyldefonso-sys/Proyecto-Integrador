package com.ojodchakra.agricultor.parcela;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/parcelas")
public class ParcelaController {

    private final ParcelaRepository repositorio;
    private final ParcelaServicio servicio;

    public ParcelaController(ParcelaRepository repositorio, ParcelaServicio servicio) {
        this.repositorio = repositorio;
        this.servicio = servicio;
    }

    @GetMapping
    public List<ParcelaRespuesta> listar() {
        return repositorio.findAllByOrderByNombreAsc().stream()
                .map(ParcelaRespuesta::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public ParcelaRespuesta obtener(@PathVariable String id) {
        return repositorio.findById(id)
                .map(ParcelaRespuesta::desde)
                .orElseThrow(() -> new ParcelaNoEncontradaException(id));
    }

    @PostMapping
    public ResponseEntity<ParcelaRespuesta> registrar(
            @Valid @RequestBody ParcelaSolicitud solicitud, HttpServletRequest request) {
        ParcelaServicio.Resultado resultado = servicio.registrar(solicitud, request.getRemoteAddr());
        HttpStatus estado = resultado.creada() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(estado).body(ParcelaRespuesta.desde(resultado.parcela()));
    }
}