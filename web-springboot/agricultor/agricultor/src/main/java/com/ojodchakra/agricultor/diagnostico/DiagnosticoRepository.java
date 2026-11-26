package com.ojodchakra.agricultor.diagnostico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DiagnosticoRepository extends JpaRepository<Diagnostico, String> {

    List<Diagnostico> findByUsuarioIdAndEstadoRevisionIn(String usuarioId, Collection<String> estados);
}
