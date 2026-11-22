package com.ojodchakra.agricultor.parcela;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParcelaController.class)
class ParcelaControllerTest {

    private static final String CLAVE = "dev-demo-key";

    private static final String CUERPO_VALIDO = """
            {"id":"3881c177-5378-4cba-bd47-04456c296e61","usuarioId":"u1","nombre":"Finca Nueva",
             "cultivo":"Papa","referencia":"Chosica","latitud":-12.05,"longitud":-77.03,
             "fechaRegistro":"2026-11-21T18:00:00Z"}
            """;

    private static final String CUERPO_LATITUD_INVALIDA = """
            {"id":"3881c177-5378-4cba-bd47-04456c296e61","usuarioId":"u1","nombre":"Finca Nueva",
             "cultivo":"Papa","latitud":200,"longitud":-77.03,"fechaRegistro":"2026-11-21T18:00:00Z"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParcelaRepository repositorio;

    @MockitoBean
    private ParcelaServicio servicio;

    private Parcela parcela(String id, String nombre) {
        return new Parcela(id, "usuario-1", nombre, "Papa", "Chosica",
                -11.93, -76.7, LocalDateTime.of(2026, 11, 1, 10, 0));
    }

    @Test
    void listaLasParcelasEnElOrdenDelRepositorio() throws Exception {
        when(repositorio.findAllByOrderByNombreAsc())
                .thenReturn(List.of(parcela("a1", "Chacra Los Andes"), parcela("b2", "Finca Sol Naciente")));

        mockMvc.perform(get("/api/parcelas").header("X-Api-Key", CLAVE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Chacra Los Andes"))
                .andExpect(jsonPath("$[1].id").value("b2"));
    }

    @Test
    void devuelveUnaParcelaPorId() throws Exception {
        when(repositorio.findById("a1")).thenReturn(Optional.of(parcela("a1", "Chacra Los Andes")));

        mockMvc.perform(get("/api/parcelas/a1").header("X-Api-Key", CLAVE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cultivo").value("Papa"))
                .andExpect(jsonPath("$.latitud").value(-11.93));
    }

    @Test
    void laFechaSaleEnFormatoIsoEnUtc() throws Exception {
        when(repositorio.findById("a1")).thenReturn(Optional.of(parcela("a1", "Chacra Los Andes")));

        mockMvc.perform(get("/api/parcelas/a1").header("X-Api-Key", CLAVE))
                .andExpect(jsonPath("$.fechaCreacion").value("2026-11-01T10:00:00Z"));
    }

    @Test
    void unIdInexistenteDevuelve404ConDetalle() throws Exception {
        when(repositorio.findById("no-existe")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/parcelas/no-existe").header("X-Api-Key", CLAVE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Parcela no encontrada"));
    }

    @Test
    void sinLaClaveDeApiDevuelve401() throws Exception {
        mockMvc.perform(get("/api/parcelas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unaClaveIncorrectaDevuelve401() throws Exception {
        mockMvc.perform(get("/api/parcelas").header("X-Api-Key", "otra-clave"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrarUnaParcelaNuevaDevuelve201() throws Exception {
        when(servicio.registrar(any(), any()))
                .thenReturn(new ParcelaServicio.Resultado(parcela("a1", "Finca Nueva"), true));

        mockMvc.perform(post("/api/parcelas")
                        .header("X-Api-Key", CLAVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Finca Nueva"));
    }

    @Test
    void reenviarUnaParcelaQueYaExisteDevuelve200() throws Exception {
        when(servicio.registrar(any(), any()))
                .thenReturn(new ParcelaServicio.Resultado(parcela("a1", "Finca Nueva"), false));

        mockMvc.perform(post("/api/parcelas")
                        .header("X-Api-Key", CLAVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isOk());
    }

    @Test
    void datosInvalidosDevuelven400ConElErrorPorCampo() throws Exception {
        mockMvc.perform(post("/api/parcelas")
                        .header("X-Api-Key", CLAVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_LATITUD_INVALIDA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.latitud").exists());
    }
}