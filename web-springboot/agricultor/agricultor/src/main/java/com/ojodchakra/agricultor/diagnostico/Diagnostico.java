package com.ojodchakra.agricultor.diagnostico;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Mapea la tabla que crea Django. Si en phpMyAdmin se llama distinto, cambia solo el @Table. */
@Entity
@Table(name = "monitoreo_diagnostico")
public class Diagnostico {

    @Id
    @Column(length = 100)
    private String id;

    @Column(name = "usuario_id", nullable = false, length = 100)
    private String usuarioId;

    @Column(name = "parcela_id", length = 100)
    private String parcelaId;

    @Column(length = 100)
    private String foto;

    @Column(name = "plaga_detectada", nullable = false, length = 150)
    private String plagaDetectada;

    @Column(nullable = false)
    private Double confianza;

    @Column(name = "nivel_severidad", nullable = false, length = 50)
    private String nivelSeveridad;

    @Column(nullable = false)
    private String recomendacion;

    @Column(name = "fecha_captura", nullable = false)
    private LocalDateTime fechaCaptura;

    protected Diagnostico() {
        // Lo exige JPA
    }

    public Diagnostico(String id, String usuarioId, String parcelaId, String foto,
                       String plagaDetectada, Double confianza, String nivelSeveridad,
                       String recomendacion, LocalDateTime fechaCaptura) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.parcelaId = parcelaId;
        this.foto = foto;
        this.plagaDetectada = plagaDetectada;
        this.confianza = confianza;
        this.nivelSeveridad = nivelSeveridad;
        this.recomendacion = recomendacion;
        this.fechaCaptura = fechaCaptura;
    }

    public String getId() { return id; }
    public String getUsuarioId() { return usuarioId; }
    public String getParcelaId() { return parcelaId; }
    public String getFoto() { return foto; }
    public String getPlagaDetectada() { return plagaDetectada; }
    public Double getConfianza() { return confianza; }
    public String getNivelSeveridad() { return nivelSeveridad; }
    public String getRecomendacion() { return recomendacion; }
    public LocalDateTime getFechaCaptura() { return fechaCaptura; }
}