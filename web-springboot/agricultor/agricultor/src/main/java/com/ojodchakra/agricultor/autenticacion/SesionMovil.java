package com.ojodchakra.agricultor.autenticacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Tabla creada por la migración 0005 de Django. Guarda solo el hash del token. */
@Entity
@Table(name = "monitoreo_sesionmovil")
public class SesionMovil {

    @Id
    @Column(length = 100)
    private String id;

    @Column(name = "usuario_id", nullable = false, length = 100)
    private String usuarioId;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "creada", nullable = false)
    private LocalDateTime creada;

    @Column(name = "expira", nullable = false)
    private LocalDateTime expira;

    protected SesionMovil() {
        // Lo exige JPA
    }

    public SesionMovil(String id, String usuarioId, String tokenHash, LocalDateTime creada, LocalDateTime expira) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.tokenHash = tokenHash;
        this.creada = creada;
        this.expira = expira;
    }

    public String getUsuarioId() { return usuarioId; }
    public LocalDateTime getExpira() { return expira; }
}
