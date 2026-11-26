package com.ojodchakra.agricultor.diagnostico;

import jakarta.servlet.http.HttpServletRequest;
import com.ojodchakra.agricultor.autenticacion.AuthServicio;
import com.ojodchakra.agricultor.error.NoAutorizadoException;
import com.ojodchakra.agricultor.error.SolicitudInvalidaException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/diagnosticos")
public class DiagnosticoController {

    private final DiagnosticoServicio servicio;
    private final AuthServicio autenticacion;

    public DiagnosticoController(DiagnosticoServicio servicio, AuthServicio autenticacion) {
        this.servicio = servicio;
        this.autenticacion = autenticacion;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiagnosticoRespuesta> registrar(
            @RequestPart("datos") @Valid DiagnosticoSolicitud datos,
            @RequestPart("foto") MultipartFile foto,
            HttpServletRequest request) {
        DiagnosticoServicio.Resultado resultado = servicio.registrar(datos, foto, request.getRemoteAddr());
        HttpStatus estado = resultado.creado() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(estado).body(DiagnosticoRespuesta.desde(resultado.diagnostico()));
    }

    /** La app consulta las revisiones ya decididas por el agrónomo para actualizar su historial. */
    @GetMapping("/revisiones")
    public List<DiagnosticoRespuesta> revisadas(
            @RequestParam("usuarioId") String usuarioId,
            @RequestHeader(value = "Authorization", required = false) String autorizacion) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new SolicitudInvalidaException("Falta el usuario de la consulta.");
        }
        if (!autenticacion.esDelUsuario(autorizacion, usuarioId)) {
            throw new NoAutorizadoException("Tu sesión expiró. Vuelve a iniciar sesión.");
        }
        return servicio.revisadasDe(usuarioId).stream().map(DiagnosticoRespuesta::desde).toList();
    }
}