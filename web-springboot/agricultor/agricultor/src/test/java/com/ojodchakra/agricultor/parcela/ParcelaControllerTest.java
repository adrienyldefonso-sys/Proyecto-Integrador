package com.ojodchakra.agricultor.parcela;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParcelaController.class)
class ParcelaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParcelaRepository repositorio;

    private Parcela parcela(String id, String nombre) {
        return new Parcela(id, "usuario-1", nombre, "Papa", "Chosica",
                -11.93, -76.7, LocalDateTime.of(2026, 11, 1, 10, 0));
    }

    @Test
    void listaLasParcelasEnElOrdenDelRepositorio() throws Exception {
        when(repositorio.findAllByOrderByNombreAsc())
                .thenReturn(List.of(parcela("a1", "Chacra Los Andes"), parcela("b2", "Finca Sol Naciente")));

        mockMvc.perform(get("/api/parcelas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Chacra Los Andes"))
                .andExpect(jsonPath("$[1].id").value("b2"));
    }

    @Test
    void devuelveUnaParcelaPorId() throws Exception {
        when(repositorio.findById("a1")).thenReturn(Optional.of(parcela("a1", "Chacra Los Andes")));

        mockMvc.perform(get("/api/parcelas/a1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cultivo").value("Papa"))
                .andExpect(jsonPath("$.latitud").value(-11.93));
    }

    @Test
    void laFechaSaleEnFormatoIsoEnUtc() throws Exception {
        when(repositorio.findById("a1")).thenReturn(Optional.of(parcela("a1", "Chacra Los Andes")));

        mockMvc.perform(get("/api/parcelas/a1"))
                .andExpect(jsonPath("$.fechaCreacion").value("2026-11-01T10:00:00Z"));
    }

    @Test
    void unIdInexistenteDevuelve404ConDetalle() throws Exception {
        when(repositorio.findById("no-existe")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/parcelas/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Parcela no encontrada"));
    }
}