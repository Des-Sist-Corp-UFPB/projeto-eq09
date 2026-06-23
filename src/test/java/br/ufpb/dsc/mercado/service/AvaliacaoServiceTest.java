package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Avaliacao;
import br.ufpb.dsc.mercado.domain.Filme;
import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.dto.AvaliacaoRequest;
import br.ufpb.dsc.mercado.repository.AvaliacaoRepository;
import br.ufpb.dsc.mercado.repository.FilmeRepository;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private FilmeRepository filmeRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LogAuditoriaService logAuditoriaService;

    @InjectMocks
    private AvaliacaoService avaliacaoService;

    private Usuario usuario;
    private Filme filme;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("john_doe", "pass", "USER");
        usuario.setId(1L);

        filme = new Filme("Inception", "Nolan", 2010, "Sci-Fi", "Action", "http://image.jpg");
        filme.setId(10L);
    }

    @Test
    void testAvaliar_NewRating_Success() {
        AvaliacaoRequest request = new AvaliacaoRequest(5);

        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme));
        when(avaliacaoRepository.findByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(Optional.empty());

        avaliacaoService.avaliar(10L, "john_doe", request);

        verify(avaliacaoRepository, times(1)).save(any(Avaliacao.class));
        verify(logAuditoriaService, times(1)).registrarLog("john_doe", "AVALIAR_FILME", "Avaliou o filme: Inception (ID: 10) com nota: 5");
    }

    @Test
    void testAvaliar_UpdateRating_Success() {
        AvaliacaoRequest request = new AvaliacaoRequest(4);
        Avaliacao existingRating = new Avaliacao(usuario, filme, 5);

        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme));
        when(avaliacaoRepository.findByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(Optional.of(existingRating));

        avaliacaoService.avaliar(10L, "john_doe", request);

        assertEquals(4, existingRating.getNota());
        verify(avaliacaoRepository, times(1)).save(existingRating);
        verify(logAuditoriaService, times(1)).registrarLog("john_doe", "AVALIAR_FILME", "Avaliou o filme: Inception (ID: 10) com nota: 4");
    }

    @Test
    void testAvaliar_UserNotFound() {
        AvaliacaoRequest request = new AvaliacaoRequest(5);

        when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            avaliacaoService.avaliar(10L, "unknown", request);
        });

        assertEquals("Usuário não encontrado: unknown", exception.getMessage());
        verify(avaliacaoRepository, never()).save(any(Avaliacao.class));
    }

    @Test
    void testAvaliar_FilmNotFound() {
        AvaliacaoRequest request = new AvaliacaoRequest(5);

        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            avaliacaoService.avaliar(99L, "john_doe", request);
        });

        assertEquals("Filme não encontrado: 99", exception.getMessage());
        verify(avaliacaoRepository, never()).save(any(Avaliacao.class));
    }
}
