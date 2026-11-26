package com.ojodchakra.agricultor.diagnostico;

import com.ojodchakra.agricultor.auditoria.EventoAuditoria;
import com.ojodchakra.agricultor.auditoria.EventoAuditoriaRepository;
import com.ojodchakra.agricultor.error.SolicitudInvalidaException;
import com.ojodchakra.agricultor.foto.AlmacenFotos;
import com.ojodchakra.agricultor.foto.TipoImagen;
import com.ojodchakra.agricultor.parcela.Parcela;
import com.ojodchakra.agricultor.parcela.ParcelaRepository;
import com.ojodchakra.agricultor.usuario.Usuario;
import com.ojodchakra.agricultor.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class DiagnosticoServicio {

    /** `creado` es false cuando el diagnóstico ya existía (un reintento de la app). */
    public record Resultado(Diagnostico diagnostico, boolean creado) {
    }

    private final DiagnosticoRepository diagnosticos;
    private final ParcelaRepository parcelas;
    private final UsuarioRepository usuarios;
    private final EventoAuditoriaRepository auditoria;
    private final AlmacenFotos almacen;

    public DiagnosticoServicio(DiagnosticoRepository diagnosticos, ParcelaRepository parcelas,
                               UsuarioRepository usuarios, EventoAuditoriaRepository auditoria,
                               AlmacenFotos almacen) {
        this.diagnosticos = diagnosticos;
        this.parcelas = parcelas;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
        this.almacen = almacen;
    }

    @Transactional
    public Resultado registrar(DiagnosticoSolicitud solicitud, MultipartFile foto, String ip) {
        Optional<Diagnostico> existente = diagnosticos.findById(solicitud.id());
        if (existente.isPresent()) {
            Diagnostico diagnostico = existente.get();
            if (!diagnostico.getUsuarioId().equals(solicitud.usuarioId())) {
                throw new SolicitudInvalidaException(
                        "Ya existe un diagnóstico con ese id que pertenece a otro usuario.");
            }
            if (solicitud.pideRevision()) {
                diagnostico.solicitarRevision();
                diagnosticos.saveAndFlush(diagnostico);
                auditoria.saveAndFlush(new EventoAuditoria(
                        "REVISION_SOLICITADA", "diagnostico", diagnostico.getId(),
                        diagnostico.getUsuarioId(), ip, "El agricultor pidió revisión de un agrónomo"));
            }
            return new Resultado(diagnostico, false);
        }

        Usuario usuario = usuarios.findById(solicitud.usuarioId())
                .orElseThrow(() -> new SolicitudInvalidaException("El usuario no existe en el servidor."));
        if (!usuario.isActivo()) {
            throw new SolicitudInvalidaException("El usuario está desactivado.");
        }
        validarParcela(solicitud);

        byte[] contenido = leer(foto);
        String ruta = AlmacenFotos.rutaRelativa(solicitud.id(), TipoImagen.extensionDe(contenido));

        Diagnostico nuevo = new Diagnostico(
                solicitud.id(),
                solicitud.usuarioId(),
                solicitud.parcelaId() == null || solicitud.parcelaId().isBlank() ? null : solicitud.parcelaId(),
                ruta,
                solicitud.plagaDetectada().trim(),
                solicitud.confianza(),
                texto(solicitud.nivelSeveridad()),
                texto(solicitud.recomendacion()),
                LocalDateTime.ofInstant(solicitud.fechaCaptura(), ZoneOffset.UTC));
        if (solicitud.pideRevision()) {
            nuevo.solicitarRevision();
        }

        // Primero la base y después el archivo: si la foto falla, la transacción se revierte.
        diagnosticos.saveAndFlush(nuevo);
        almacen.guardar(ruta, contenido);
        auditoria.saveAndFlush(new EventoAuditoria(
                "DIAGNOSTICO_CREADO", "diagnostico", nuevo.getId(), nuevo.getUsuarioId(), ip,
                "Diagnóstico recibido: " + nuevo.getPlagaDetectada() + " ("
                        + String.format(Locale.US, "%.1f", nuevo.getConfianza()) + " %)"));

        return new Resultado(nuevo, true);
    }

    /** Diagnósticos del usuario que el agrónomo ya confirmó o corrigió (para que la app los actualice). */
    @Transactional(readOnly = true)
    public List<Diagnostico> revisadasDe(String usuarioId) {
        return diagnosticos.findByUsuarioIdAndEstadoRevisionIn(usuarioId, List.of("CONFIRMADA", "CORREGIDA"));
    }

    private void validarParcela(DiagnosticoSolicitud solicitud) {
        if (solicitud.parcelaId() == null || solicitud.parcelaId().isBlank()) {
            return;
        }
        Parcela parcela = parcelas.findById(solicitud.parcelaId())
                .orElseThrow(() -> new SolicitudInvalidaException(
                        "La parcela no existe en el servidor. Sincroniza primero las parcelas."));
        if (!parcela.getUsuarioId().equals(solicitud.usuarioId())) {
            throw new SolicitudInvalidaException("La parcela pertenece a otro usuario.");
        }
    }

    private static byte[] leer(MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            throw new SolicitudInvalidaException("La foto es obligatoria.");
        }
        try {
            return foto.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la foto recibida", e);
        }
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}