package com.ojodchakra.agricultor.diagnostico;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DiagnosticoController.class)
class DiagnosticoControllerTest {

    private static final String CLAVE = "dev-demo-key";
    private static final String ID = "3881c177-5378-4cba-bd47-04456c296e61";

    private static final String DATOS_VALIDOS = """
            {"id":"3881c177-5378-4cba-bd47-04456c296e61","usuarioId":"u1","parcelaId":null,
             "plagaDetectada":"Potato___Late_blight","confianza":97.5,"nivelSeveridad":"ALTA",
             "recomendacion":"Actúa de inmediato","fechaCaptura":"2026-11-21T18:00:00Z"}
            """;

    private static final String DATOS_CONFIANZA_INVALIDA = """
            {"id":"3881c177-5378-4cba-bd47-04456c296e61","usuarioId":"u1",
             "plagaDetectada":"Potato___Late_blight","confianza":150,
             "fechaCaptura":"2026-11-21T18:00:00Z"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DiagnosticoServicio servicio;

    private MockMultipartFile datos(String json) {
        return new MockMultipartFile("datos", "datos.json", "application/json",
                json.getBytes(StandardCharsets.UTF_8));
    }

    private MockMultipartFile foto() {
        return new MockMultipartFile("foto", "hoja.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2});
    }

    private Diagnostico diagnostico() {
        return new Diagnostico(ID, "u1", null, "diagnosticos/" + ID + ".jpg", "Potato___Late_blight",
                97.5, "ALTA", "Actúa de inmediato", LocalDateTime.of(2026, 11, 21, 18, 0));
    }

    @Test
    void registrarUnDiagnosticoNuevoDevuelve201() throws Exception {
        when(servicio.registrar(any(), any(), any()))
                .thenReturn(new DiagnosticoServicio.Resultado(diagnostico(), true));

        mockMvc.perform(multipart("/api/diagnosticos")
                        .file(datos(DATOS_VALIDOS))
                        .file(foto())
                        .header("X-Api-Key", CLAVE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plagaDetectada").value("Potato___Late_blight"))
                .andExpect(jsonPath("$.foto").value("diagnosticos/" + ID + ".jpg"));
    }

    @Test
    void reenviarUnDiagnosticoExistenteDevuelve200() throws Exception {
        when(servicio.registrar(any(), any(), any()))
                .thenReturn(new DiagnosticoServicio.Resultado(diagnostico(), false));

        mockMvc.perform(multipart("/api/diagnosticos")
                        .file(datos(DATOS_VALIDOS))
                        .file(foto())
                        .header("X-Api-Key", CLAVE))
                .andExpect(status().isOk());
    }

    @Test
    void unaConfianzaFueraDeRangoDevuelve400ConElErrorPorCampo() throws Exception {
        mockMvc.perform(multipart("/api/diagnosticos")
                        .file(datos(DATOS_CONFIANZA_INVALIDA))
                        .file(foto())
                        .header("X-Api-Key", CLAVE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.confianza").exists());
    }

    @Test
    void sinLaClaveDeApiDevuelve401() throws Exception {
        mockMvc.perform(multipart("/api/diagnosticos")
                        .file(datos(DATOS_VALIDOS))
                        .file(foto()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinLaFotoDevuelve400() throws Exception {
        mockMvc.perform(multipart("/api/diagnosticos")
                        .file(datos(DATOS_VALIDOS))
                        .header("X-Api-Key", CLAVE))
                .andExpect(status().isBadRequest());
    }
}