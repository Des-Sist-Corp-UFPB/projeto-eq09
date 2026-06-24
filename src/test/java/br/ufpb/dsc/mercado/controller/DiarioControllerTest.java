package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.dto.*;
import br.ufpb.dsc.mercado.service.DiarioFilmeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DiarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiarioFilmeService diarioFilmeService;

    @Test
    @WithMockUser(username = "john_doe")
    void testMarcarComoAssistido_Success() throws Exception {
        Mockito.doNothing().when(diarioFilmeService).marcarComoAssistido(eq(1L), eq("john_doe"), any(DiarioRequest.class));

        mockMvc.perform(post("/api/diario/filmes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observacao\":\"Excelente filme!\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john_doe")
    void testMarcarComoAssistido_Success_NullBody() throws Exception {
        Mockito.doNothing().when(diarioFilmeService).marcarComoAssistido(eq(1L), eq("john_doe"), any(DiarioRequest.class));

        mockMvc.perform(post("/api/diario/filmes/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john_doe")
    void testMarcarComoAssistido_BadRequest() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Filme não encontrado")).when(diarioFilmeService).marcarComoAssistido(eq(1L), eq("john_doe"), any(DiarioRequest.class));

        mockMvc.perform(post("/api/diario/filmes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observacao\":\"Excelente filme!\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john_doe")
    void testRemoverMarcacao_Success() throws Exception {
        Mockito.doNothing().when(diarioFilmeService).removerMarcacao(eq(1L), eq("john_doe"));

        mockMvc.perform(delete("/api/diario/filmes/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john_doe")
    void testRemoverMarcacao_BadRequest() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Marcação não encontrada")).when(diarioFilmeService).removerMarcacao(eq(1L), eq("john_doe"));

        mockMvc.perform(delete("/api/diario/filmes/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john_doe")
    void testListarDiario_Success() throws Exception {
        DiarioResponse response = new DiarioResponse(1L, 2L, "Filme Teste", "http://imagem.com/1.jpg", Instant.now(), "Muito bom!", 5);
        Mockito.when(diarioFilmeService.listarDiario(eq("john_doe"))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/diario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Filme Teste"))
                .andExpect(jsonPath("$[0].observacao").value("Muito bom!"));
    }

    @Test
    @WithMockUser(username = "john_doe")
    void testListarDiario_BadRequest() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Usuário inválido")).when(diarioFilmeService).listarDiario(eq("john_doe"));

        mockMvc.perform(get("/api/diario"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john_doe")
    void testVerificarStatus_Success() throws Exception {
        DiarioStatusResponse response = new DiarioStatusResponse(true, Instant.now(), "Gostei");
        Mockito.when(diarioFilmeService.verificarStatus(eq(1L), eq("john_doe"))).thenReturn(response);

        mockMvc.perform(get("/api/diario/filmes/1/verificar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assistido").value(true))
                .andExpect(jsonPath("$.observacao").value("Gostei"));
    }

    @Test
    @WithMockUser(username = "john_doe")
    void testVerificarStatus_BadRequest() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Erro ao verificar")).when(diarioFilmeService).verificarStatus(eq(1L), eq("john_doe"));

        mockMvc.perform(get("/api/diario/filmes/1/verificar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john_doe")
    void testObterEstatisticas_Success() throws Exception {
        DiarioEstatisticasResponse response = new DiarioEstatisticasResponse(10L, 5L, 4.5);
        Mockito.when(diarioFilmeService.obterEstatisticas(eq("john_doe"))).thenReturn(response);

        mockMvc.perform(get("/api/diario/estatisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAssistidos").value(10))
                .andExpect(jsonPath("$.notaMedia").value(4.5));
    }

    @Test
    @WithMockUser(username = "john_doe")
    void testObterEstatisticas_BadRequest() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Erro de estatísticas")).when(diarioFilmeService).obterEstatisticas(eq("john_doe"));

        mockMvc.perform(get("/api/diario/estatisticas"))
                .andExpect(status().isBadRequest());
    }
}
