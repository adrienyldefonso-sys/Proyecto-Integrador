package com.ojodchakra.agricultor.autenticacion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SesionMovilRepository extends JpaRepository<SesionMovil, String> {

    Optional<SesionMovil> findByTokenHash(String tokenHash);
}
