package com.ojodchakra.agricultor.diagnostico;

import com.ojodchakra.agricultor.auditoria.EventoAuditoria;
import com.ojodchakra.agricultor.auditoria.EventoAuditoriaRepository;
import com.ojodchakra.agricultor.error.SolicitudInvalidaException;
import com.ojodchakra.agricultor.foto.AlmacenFotos;
import com.ojodchakra.agricultor.parcela.Parcela;
import com.ojodchakra.agricultor.parcela.ParcelaRepository;
import com.ojodchakra.agricultor.usuario.Usuario;
import com.ojodchakra.agricultor.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DiagnosticoServicioTest {

    private static final String ID = "3881c177-5378-4cba-bd47-04456c296e61";
    private static final String PARCELA = "59f18f42-5e0a-4037-84be-137bdd0e52f9";
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 1, 2, 3};
    private static final String RUTA = "diagnosticos/" + ID + ".jpg";

    @TempDir
    Path carpeta;

    @Mock
    private DiagnosticoRepository diagnosticos;

    @Mock
    private ParcelaRepository parcelas;

    @Mock
    private UsuarioRepository usuarios;

    @Mock
    private EventoAuditoriaRepository auditoria;

    private AlmacenFotos almacen;
    private DiagnosticoServicio servicio;

    @BeforeEach
    void preparar() {
        almacen = new AlmacenFotos(carpeta.toString());
        servicio = new DiagnosticoServicio(diagnosticos, parcelas, usuarios, auditoria, almacen);
        when(diagnosticos.findById(any())).thenReturn(Optional.empty());
        when(usuarios.findById("u1")).thenReturn(Optional.of(new Usuario("u1", true)));
        when(parcelas.findById(PARCELA)).thenReturn(Optional.of(parcela("u1")));
    }

    private Parcela parcela(String usuarioId) {
        return new Parcela(PARCELA, usuarioId, "Finca", "Papa", null,
                -12.0, -77.0, LocalDateTime.of(2026, 11, 1, 10, 0));
    }

    private Diagnostico existente(String usuarioId) {
        return new Diagnostico(ID, usuarioId, PARCELA, RUTA, "Potato___Late_blight",
                97.5, "ALTA", "x", LocalDateTime.of(2026, 11, 21, 18, 0));
    }

    private DiagnosticoSolicitud solicitud() {
        return new DiagnosticoSolicitud(ID, "u1", PARCELA, "Potato___Late_blight", 97.5,
                "ALTA", "Actúa de inmediato", Instant.parse("2026-11-21T18:00:00Z"));
    }

    private MockMultipartFile foto(byte[] contenido) {
        return new MockMultipartFile("foto", "hoja.jpg", "image/jpeg", contenido);
    }

    @Test
    void creaElDiagnosticoGuardaLaFotoYAudita() {
        DiagnosticoServicio.Resultado resultado = servicio.registrar(solicitud(), foto(JPEG), "127.0.0.1");

        assertTrue(resultado.creado());
        assertEquals(RUTA, resultado.diagnostico().getFoto());
        assertTrue(almacen.existe(RUTA));
        verify(diagnosticos).saveAndFlush(any(Diagnostico.class));
        verify(auditoria).saveAndFlush(any(EventoAuditoria.class));
    }

    @Test
    void reenviarElMismoDiagnosticoNoLoDuplicaNiReescribeLaFoto() {
        when(diagnosticos.findById(ID)).thenReturn(Optional.of(existente("u1")));

        DiagnosticoServicio.Resultado resultado = servicio.registrar(solicitud(), foto(JPEG), "127.0.0.1");

        assertFalse(resultado.creado());
        verify(diagnosticos, never()).saveAndFlush(any(Diagnostico.class));
        verifyNoInteractions(auditoria);
        assertFalse(almacen.existe(RUTA));
    }

    @Test
    void unIdQuePerteneceAOtroUsuarioSeRechaza() {
        when(diagnosticos.findById(ID)).thenReturn(Optional.of(existente("otro-usuario")));

        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.registrar(solicitud(), foto(JPEG), "127.0.0.1"));
    }

    @Test
    void unUsuarioInexistenteSeRechazaYNoGuardaLaFoto() {
        when(usuarios.findById("u1")).thenReturn(Optional.empty());

        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.registrar(solicitud(), foto(JPEG), "127.0.0.1"));
        assertFalse(almacen.existe(RUTA));
    }

    @Test
    void unaParcelaInexistenteSeRechaza() {
        when(parcelas.findById(PARCELA)).thenReturn(Optional.empty());

        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.registrar(solicitud(), foto(JPEG), "127.0.0.1"));
    }

    @Test
    void unaParcelaDeOtroUsuarioSeRechaza() {
        when(parcelas.findById(PARCELA)).thenReturn(Optional.of(parcela("otro-usuario")));

        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.registrar(solicitud(), foto(JPEG), "127.0.0.1"));
    }

    @Test
    void unArchivoQueNoEsImagenSeRechazaYNoSeGuardaNada() {
        byte[] texto = "esto no es una imagen".getBytes(StandardCharsets.UTF_8);

        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.registrar(solicitud(), foto(texto), "127.0.0.1"));
        verify(diagnosticos, never()).saveAndFlush(any(Diagnostico.class));
        assertFalse(almacen.existe(RUTA));
    }

    @Test
    void unaFotoVaciaSeRechaza() {
        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.registrar(solicitud(), foto(new byte[0]), "127.0.0.1"));
    }
}