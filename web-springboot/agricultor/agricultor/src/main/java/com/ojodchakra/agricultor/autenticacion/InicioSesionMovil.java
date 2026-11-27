package com.ojodchakra.agricultor.autenticacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Tabla creada por la migración 0006 de Django. Un registro por cada inicio de sesión desde la app. */
@Entity
@Table(name = "monitoreo_iniciosesionmovil")
public class InicioSesionMovil {

    @Id
    @Column(length = 100)
    private String id;

    @Column(name = "usuario_id", nullable = false, length = 100)
    private String usuarioId;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    protected InicioSesionMovil() {
        // Lo exige JPA
    }

    public InicioSesionMovil(String id, String usuarioId, LocalDateTime fecha) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.fecha = fecha;
    }
}
