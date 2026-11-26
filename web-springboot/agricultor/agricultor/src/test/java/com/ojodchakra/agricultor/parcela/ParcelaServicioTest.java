package com.ojodchakra.agricultor.parcela;

import com.ojodchakra.agricultor.auditoria.EventoAuditoria;
import com.ojodchakra.agricultor.auditoria.EventoAuditoriaRepository;
import com.ojodchakra.agricultor.error.SolicitudInvalidaException;
import com.ojodchakra.agricultor.usuario.Usuario;
import com.ojodchakra.agricultor.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParcelaServicioTest {

    private static final String ID = "3881c177-5378-4cba-bd47-04456c296e61";

    @Mock
    private ParcelaRepository parcelas;

    @Mock
    private UsuarioRepository usuarios;

    @Mock
    private EventoAuditoriaRepository auditoria;

    private ParcelaServicio servicio;

    @BeforeEach
    void preparar() {
        servicio = new ParcelaServicio(parcelas, usuarios, auditoria);
    }

    private ParcelaSolicitud solicitud() {
        return new ParcelaSolicitud(ID, "u1", " Finca Nueva ", "Papa", "",
                -12.05, -77.03, Instant.parse("2026-11-21T18:00:00Z"));
    }

    private Parcela existente(String usuarioId) {
        return new Parcela(ID, usuarioId, "Finca Nueva", "Papa", null,
                -12.05, -77.03, LocalDateTime.of(2026, 11, 21, 18, 0));
    }

    @Test
    void creaLaParcelaNuevaYRegistraLaAuditoria() {
        when(parcelas.findById(any())).thenReturn(Optional.empty());
        when(usuarios.findById("u1")).thenReturn(Optional.of(new Usuario("u1", true)));

        ParcelaServicio.Resultado resultado = servicio.registrar(solicitud(), "127.0.0.1");

        assertTrue(resultado.creada());
        assertEquals("Finca Nueva", resultado.parcela().getNombre());
        assertNull(resultado.parcela().getReferencia());
        verify(parcelas).saveAndFlush(any(Parcela.class));
        verify(auditoria).save(any(EventoAuditoria.class));
    }

    @Test
    void reenviarLaMismaParcelaNoLaDuplicaNiLaAudita() {
        when(parcelas.findById(any())).thenReturn(Optional.of(existente("u1")));

        ParcelaServicio.Resultado resultado = servicio.registrar(solicitud(), "127.0.0.1");

        assertFalse(resultado.creada());
        verify(parcelas, never()).saveAndFlush(any(Parcela.class));
        verifyNoInteractions(auditoria);
    }

    @Test
    void unIdQueYaPerteneceAOtroUsuarioSeRechaza() {
        when(parcelas.findById(any())).thenReturn(Optional.of(existente("otro-usuario")));

        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.registrar(solicitud(), "127.0.0.1"));
    }

    @Test
    void unUsuarioInexistenteSeRechazaYNoSeGuardaNada() {
        when(parcelas.findById(any())).thenReturn(Optional.empty());
        when(usuarios.findById("u1")).thenReturn(Optional.empty());

        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.registrar(solicitud(), "127.0.0.1"));
        verify(parcelas, never()).saveAndFlush(any(Parcela.class));
    }

    @Test
    void unUsuarioDesactivadoSeRechaza() {
        when(parcelas.findById(any())).thenReturn(Optional.empty());
        when(usuarios.findById("u1")).thenReturn(Optional.of(new Usuario("u1", false)));

        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.registrar(solicitud(), "127.0.0.1"));
    }

    @Test
    void unaParcelaSinCoordenadasSeCreaSinUbicacion() {
        when(parcelas.findById(any())).thenReturn(Optional.empty());
        when(usuarios.findById("u1")).thenReturn(Optional.of(new Usuario("u1", true)));
        ParcelaSolicitud sinUbicacion = new ParcelaSolicitud(ID, "u1", "Finca", "Papa", "",
                null, null, Instant.parse("2026-11-21T18:00:00Z"));

        ParcelaServicio.Resultado resultado = servicio.registrar(sinUbicacion, "127.0.0.1");

        assertTrue(resultado.creada());
        assertNull(resultado.parcela().getLatitud());
        assertNull(resultado.parcela().getLongitud());
        verify(parcelas).saveAndFlush(any(Parcela.class));
    }

    @Test
    void soloUnaCoordenadaSeRechazaYNoSeGuardaNada() {
        ParcelaSolicitud soloLatitud = new ParcelaSolicitud(ID, "u1", "Finca", "Papa", "",
                -12.05, null, Instant.parse("2026-11-21T18:00:00Z"));

        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.registrar(soloLatitud, "127.0.0.1"));
        verify(parcelas, never()).saveAndFlush(any(Parcela.class));
    }
}
