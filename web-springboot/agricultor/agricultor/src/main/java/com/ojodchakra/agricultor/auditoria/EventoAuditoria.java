package com.ojodchakra.agricultor.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** La tabla la crea Django; Spring Boot solo escribe. */
@Entity
@Table(name = "monitoreo_eventoauditoria")
public class EventoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 50)
    private String accion;

    @Column(nullable = false, length = 50)
    private String entidad;

    @Column(name = "entidad_id", nullable = false, length = 100)
    private String entidadId;

    @Column(name = "usuario_id", nullable = false, length = 100)
    private String usuarioId;

    @Column(nullable = false, length = 30)
    private String origen;

    @Column(nullable = false, length = 45)
    private String ip;

    @Column(nullable = false)
    private String detalle;

    protected EventoAuditoria() {
        // Lo exige JPA
    }

    public EventoAuditoria(String accion, String entidad, String entidadId,
                           String usuarioId, String ip, String detalle) {
        this.fecha = LocalDateTime.now(ZoneOffset.UTC);
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.usuarioId = usuarioId == null ? "" : usuarioId;
        this.origen = "springboot";
        this.ip = ip == null ? "" : ip;
        this.detalle = detalle == null ? "" : detalle;
    }

    public Long getId() { return id; }
    public String getAccion() { return accion; }
    public String getEntidadId() { return entidadId; }
}