package com.ojodchakra.agricultor.parcela;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/parcelas")
public class ParcelaController {

    private final ParcelaRepository repositorio;

    public ParcelaController(ParcelaRepository repositorio) {
        this.repositorio = repositorio;
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
}