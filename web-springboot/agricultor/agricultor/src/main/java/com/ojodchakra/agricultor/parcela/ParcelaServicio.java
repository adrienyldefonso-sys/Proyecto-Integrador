package com.ojodchakra.agricultor.parcela;

import com.ojodchakra.agricultor.auditoria.EventoAuditoria;
import com.ojodchakra.agricultor.auditoria.EventoAuditoriaRepository;
import com.ojodchakra.agricultor.error.SolicitudInvalidaException;
import com.ojodchakra.agricultor.usuario.Usuario;
import com.ojodchakra.agricultor.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class ParcelaServicio {

    /** `creada` es false cuando la parcela ya existía (un reintento de la app). */
    public record Resultado(Parcela parcela, boolean creada) {
    }

    private final ParcelaRepository parcelas;
    private final UsuarioRepository usuarios;
    private final EventoAuditoriaRepository auditoria;

    public ParcelaServicio(ParcelaRepository parcelas, UsuarioRepository usuarios,
                           EventoAuditoriaRepository auditoria) {
        this.parcelas = parcelas;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
    }

    @Transactional
    public Resultado registrar(ParcelaSolicitud solicitud, String ip) {
        if ((solicitud.latitud() == null) != (solicitud.longitud() == null)) {
            throw new SolicitudInvalidaException(
                    "Envía latitud y longitud juntas, o ninguna (parcela sin ubicación).");
        }

        Optional<Parcela> existente = parcelas.findById(solicitud.id());
        if (existente.isPresent()) {
            Parcela parcela = existente.get();
            if (!parcela.getUsuarioId().equals(solicitud.usuarioId())) {
                throw new SolicitudInvalidaException(
                        "Ya existe una parcela con ese id que pertenece a otro usuario.");
            }
            return new Resultado(parcela, false);
        }

        Usuario usuario = usuarios.findById(solicitud.usuarioId())
                .orElseThrow(() -> new SolicitudInvalidaException("El usuario no existe en el servidor."));
        if (!usuario.isActivo()) {
            throw new SolicitudInvalidaException("El usuario está desactivado.");
        }

        Parcela nueva = new Parcela(
                solicitud.id(),
                solicitud.usuarioId(),
                solicitud.nombre().trim(),
                solicitud.cultivo().trim(),
                nulaSiVacia(solicitud.referencia()),
                solicitud.latitud(),
                solicitud.longitud(),
                LocalDateTime.ofInstant(solicitud.fechaRegistro(), ZoneOffset.UTC));

        parcelas.saveAndFlush(nueva);
        auditoria.save(new EventoAuditoria(
                "PARCELA_CREADA", "parcela", nueva.getId(), nueva.getUsuarioId(), ip,
                "Parcela registrada desde la app: " + nueva.getNombre()));

        return new Resultado(nueva, true);
    }

    private static String nulaSiVacia(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}