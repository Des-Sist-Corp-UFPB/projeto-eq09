package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.DiarioFilme;
import br.ufpb.dsc.mercado.domain.Filme;
import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.dto.ChatResponse;
import br.ufpb.dsc.mercado.repository.DiarioFilmeRepository;
import br.ufpb.dsc.mercado.repository.FilmeRepository;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private FilmeRepository filmeRepository;

    @Mock
    private DiarioFilmeRepository diarioFilmeRepository;

    @InjectMocks
    private ChatbotService chatbotService;

    private Usuario usuario;
    private Filme filme1;
    private Filme filme2;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("iury", "pass123", "USER");
        usuario.setId(1L);

        filme1 = new Filme("Inception", "Christopher Nolan", 2010, "Sinopse 1", "Ficção Científica", "http://img1.jpg");
        filme1.setId(10L);

        filme2 = new Filme("Interstellar", "Christopher Nolan", 2014, "Sinopse 2", "Ficção Científica", "http://img2.jpg");
        filme2.setId(20L);
    }

    @Test
    void testConversar_BlankMessage() {
        ChatResponse response = chatbotService.conversar("iury", "  ");
        assertNotNull(response);
        assertTrue(response.resposta().contains("Como posso ajudar"));
        assertTrue(response.recomendacoes().isEmpty());
    }

    @Test
    void testConversar_WithWatchedHistory() {
        DiarioFilme diario = new DiarioFilme(usuario, filme1, "Excelente filme!");
        
        when(usuarioRepository.findByUsername("iury")).thenReturn(Optional.of(usuario));
        when(diarioFilmeRepository.findByUsuarioIdOrderByDataAssistidoDesc(1L)).thenReturn(List.of(diario));
        when(filmeRepository.findAll()).thenReturn(List.of(filme1, filme2));

        ChatResponse response = chatbotService.conversar("iury", "Me recomende um filme");

        assertNotNull(response);
        assertTrue(response.resposta().contains("Analisando o seu histórico de diário"));
        assertFalse(response.recomendacoes().isEmpty());
        assertEquals("Interstellar", response.recomendacoes().get(0).titulo());
    }

    @Test
    void testConversar_GenreFilter() {
        when(usuarioRepository.findByUsername("iury")).thenReturn(Optional.of(usuario));
        when(diarioFilmeRepository.findByUsuarioIdOrderByDataAssistidoDesc(1L)).thenReturn(Collections.emptyList());
        when(filmeRepository.findAll()).thenReturn(List.of(filme1, filme2));

        ChatResponse response = chatbotService.conversar("iury", "Quero assistir uma ficção científica");

        assertNotNull(response);
        assertTrue(response.resposta().contains("Ficção Científica"));
        assertEquals(2, response.recomendacoes().size());
    }
}
