package com.ojodchakra.agricultor.autenticacion;

import com.ojodchakra.agricultor.error.ConflictoRegistroException;
import com.ojodchakra.agricultor.error.NoAutorizadoException;
import com.ojodchakra.agricultor.usuario.HashContrasena;
import com.ojodchakra.agricultor.usuario.Usuario;
import com.ojodchakra.agricultor.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthServicio {

    private static final int DIAS_SESION = 30;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final UsuarioRepository usuarios;
    private final SesionMovilRepository sesiones;

    public AuthServicio(UsuarioRepository usuarios, SesionMovilRepository sesiones) {
        this.usuarios = usuarios;
        this.sesiones = sesiones;
    }

    @Transactional
    public RespuestaAuth registrar(SolicitudRegistro solicitud) {
        String usuario = normalizar(solicitud.usuario());
        if (usuarios.existsByUsername(usuario)) {
            throw new ConflictoRegistroException("Ese nombre de usuario ya está registrado.");
        }
        Usuario nuevo = new Usuario(
                UUID.randomUUID().toString(),
                usuario,
                HashContrasena.generar(solicitud.contrasena()),
                solicitud.nombre().trim(),
                blancoANulo(solicitud.telefono()));
        usuarios.save(nuevo);
        return iniciarSesion(nuevo);
    }

    @Transactional
    public RespuestaAuth login(SolicitudLogin solicitud) {
        Usuario usuario = usuarios.findByUsername(normalizar(solicitud.usuario()))
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new NoAutorizadoException("Usuario o contraseña incorrectos."));
        if (!HashContrasena.verificar(solicitud.contrasena(), usuario.getContrasena())) {
            throw new NoAutorizadoException("Usuario o contraseña incorrectos.");
        }
        return iniciarSesion(usuario);
    }

    @Transactional
    public void cerrarSesion(String token) {
        if (token == null) {
            return;
        }
        sesiones.findByTokenHash(hashToken(token)).ifPresent(sesiones::delete);
    }

    /** Dice si el token pertenece al usuario y todavía no venció. */
    @Transactional(readOnly = true)
    public boolean esDelUsuario(String autorizacion, String usuarioId) {
        String token = extraerToken(autorizacion);
        if (token == null || usuarioId == null) {
            return false;
        }
        return sesiones.findByTokenHash(hashToken(token))
                .filter(sesion -> sesion.getExpira().isAfter(ahora()))
                .map(sesion -> sesion.getUsuarioId().equals(usuarioId))
                .orElse(false);
    }

    /** Saca el token de un encabezado "Authorization: Bearer ...". */
    static String extraerToken(String autorizacion) {
        if (autorizacion == null || !autorizacion.startsWith("Bearer ")) {
            return null;
        }
        String token = autorizacion.substring("Bearer ".length()).trim();
        return token.isEmpty() ? null : token;
    }

    private RespuestaAuth iniciarSesion(Usuario usuario) {
        String token = generarToken();
        LocalDateTime ahora = ahora();
        LocalDateTime expira = ahora.plusDays(DIAS_SESION);
        sesiones.save(new SesionMovil(
                UUID.randomUUID().toString(), usuario.getId(), hashToken(token), ahora, expira));
        return new RespuestaAuth(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getRol(),
                token,
                expira.toInstant(ZoneOffset.UTC));
    }

    private static LocalDateTime ahora() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    private static String generarToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** El servidor guarda solo el hash del token; si la base se filtra, los tokens no sirven. */
    private static String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible.", e);
        }
    }

    private static String normalizar(String usuario) {
        return usuario.trim().toLowerCase(Locale.ROOT);
    }

    private static String blancoANulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
