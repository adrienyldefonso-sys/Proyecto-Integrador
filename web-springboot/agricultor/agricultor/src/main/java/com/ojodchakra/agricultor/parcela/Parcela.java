package com.ojodchakra.agricultor.parcela;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Mapea la tabla que crea Django. Si en phpMyAdmin se llama distinto, cambia solo el @Table. */
@Entity
@Table(name = "monitoreo_parcela")
public class Parcela {

    @Id
    @Column(length = 100)
    private String id;

    @Column(name = "usuario_id", nullable = false, length = 100)
    private String usuarioId;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 100)
    private String cultivo;

    @Column(length = 255)
    private String referencia;

    private Double latitud;

    private Double longitud;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    protected Parcela() {
        // Lo exige JPA
    }

    public Parcela(String id, String usuarioId, String nombre, String cultivo, String referencia,
                   Double latitud, Double longitud, LocalDateTime fechaCreacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.cultivo = cultivo;
        this.referencia = referencia;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fechaCreacion = fechaCreacion;
    }

    public String getId() { return id; }
    public String getUsuarioId() { return usuarioId; }
    public String getNombre() { return nombre; }
    public String getCultivo() { return cultivo; }
    public String getReferencia() { return referencia; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}