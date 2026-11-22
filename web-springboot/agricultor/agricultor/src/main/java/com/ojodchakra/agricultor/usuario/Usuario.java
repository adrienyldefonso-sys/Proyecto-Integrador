package com.ojodchakra.agricultor.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "monitoreo_usuario")
public class Usuario {

    @Id
    @Column(length = 100)
    private String id;

    @Column(name = "is_active", nullable = false)
    private boolean activo;

    protected Usuario() {
        // Lo exige JPA
    }

    public Usuario(String id, boolean activo) {
        this.id = id;
        this.activo = activo;
    }

    public String getId() { return id; }
    public boolean isActivo() { return activo; }
}