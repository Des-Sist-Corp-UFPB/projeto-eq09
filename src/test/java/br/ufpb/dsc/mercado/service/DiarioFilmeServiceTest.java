package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Avaliacao;
import br.ufpb.dsc.mercado.domain.DiarioFilme;
import br.ufpb.dsc.mercado.domain.Filme;
import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.dto.*;
import br.ufpb.dsc.mercado.repository.AvaliacaoRepository;
import br.ufpb.dsc.mercado.repository.DiarioFilmeRepository;
import br.ufpb.dsc.mercado.repository.FilmeRepository;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import br.ufpb.dsc.mercado.repository.WatchlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiarioFilmeServiceTest {

    @Mock
    private DiarioFilmeRepository diarioFilmeRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private FilmeRepository filmeRepository;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private LogAuditoriaService logAuditoriaService;

    @InjectMocks
    private DiarioFilmeService diarioFilmeService;

    private Usuario usuario;
    private Filme filme;
    private DiarioFilme diarioFilme;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("john_doe", "pass", "USER");
        usuario.setId(1L);

        filme = new Filme("Inception", "Nolan", 2010, "Sci-Fi", "Action", "http://image.jpg");
        filme.setId(10L);

        diarioFilme = new DiarioFilme(usuario, filme, "Great experience.");
        diarioFilme.setId(100L);
    }

    @Test
    void testMarcarComoAssistido_NewMarking_Success() {
        DiarioRequest request = new DiarioRequest("Good watch!");
        
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme));
        when(diarioFilmeRepository.findByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(Optional.empty());

        diarioFilmeService.marcarComoAssistido(10L, "john_doe", request);

        verify(watchlistRepository, times(1)).deleteByUsuarioIdAndFilmeId(1L, 10L);
        verify(diarioFilmeRepository, times(1)).save(any(DiarioFilme.class));
        verify(logAuditoriaService, times(1)).registrarLog("john_doe", "MARCAR_ASSISTIDO", "Marcou o filme como assistido: Inception (ID: 10)");
    }

    @Test
    void testMarcarComoAssistido_ExistingMarking_Success() {
        DiarioRequest request = new DiarioRequest("Updated comment.");
        
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme));
        when(diarioFilmeRepository.findByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(Optional.of(diarioFilme));

        diarioFilmeService.marcarComoAssistido(10L, "john_doe", request);

        assertEquals("Updated comment.", diarioFilme.getObservacao());
        verify(diarioFilmeRepository, times(1)).save(diarioFilme);
        verify(logAuditoriaService, times(1)).registrarLog("john_doe", "MARCAR_ASSISTIDO", "Marcou o filme como assistido: Inception (ID: 10)");
    }

    @Test
    void testMarcarComoAssistido_UserNotFound() {
        DiarioRequest request = new DiarioRequest("No watch");

        when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            diarioFilmeService.marcarComoAssistido(10L, "unknown", request);
        });

        assertEquals("Usuário não encontrado: unknown", exception.getMessage());
    }

    @Test
    void testMarcarComoAssistido_FilmNotFound() {
        DiarioRequest request = new DiarioRequest("No watch");

        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            diarioFilmeService.marcarComoAssistido(99L, "john_doe", request);
        });

        assertEquals("Filme não encontrado: 99", exception.getMessage());
    }

    @Test
    void testRemoverMarcacao_Success() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme));
        when(diarioFilmeRepository.findByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(Optional.of(diarioFilme));

        diarioFilmeService.removerMarcacao(10L, "john_doe");

        verify(diarioFilmeRepository, times(1)).delete(diarioFilme);
        verify(logAuditoriaService, times(1)).registrarLog("john_doe", "REMOVER_ASSISTIDO", "Removeu marcação de assistido do filme: Inception (ID: 10)");
    }

    @Test
    void testRemoverMarcacao_UserNotFound() {
        when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            diarioFilmeService.removerMarcacao(10L, "unknown");
        });

        assertEquals("Usuário não encontrado: unknown", exception.getMessage());
    }

    @Test
    void testRemoverMarcacao_FilmNotFound() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            diarioFilmeService.removerMarcacao(99L, "john_doe");
        });

        assertEquals("Filme não encontrado: 99", exception.getMessage());
    }

    @Test
    void testRemoverMarcacao_MarkingNotFound() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme));
        when(diarioFilmeRepository.findByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            diarioFilmeService.removerMarcacao(10L, "john_doe");
        });

        assertEquals("Filme não está marcado como assistido por este usuário.", exception.getMessage());
    }

    @Test
    void testListarDiario_Success_WithRating() {
        Avaliacao rating = new Avaliacao(usuario, filme, 4);

        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(diarioFilmeRepository.findByUsuarioIdOrderByDataAssistidoDesc(1L)).thenReturn(List.of(diarioFilme));
        when(avaliacaoRepository.findByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(Optional.of(rating));

        List<DiarioResponse> result = diarioFilmeService.listarDiario("john_doe");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).filmeId());
        assertEquals(4, result.get(0).notaAvaliacao());
    }

    @Test
    void testListarDiario_Success_WithoutRating() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(diarioFilmeRepository.findByUsuarioIdOrderByDataAssistidoDesc(1L)).thenReturn(List.of(diarioFilme));
        when(avaliacaoRepository.findByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(Optional.empty());

        List<DiarioResponse> result = diarioFilmeService.listarDiario("john_doe");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).notaAvaliacao());
    }

    @Test
    void testListarDiario_UserNotFound() {
        when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            diarioFilmeService.listarDiario("unknown");
        });

        assertEquals("Usuário não encontrado: unknown", exception.getMessage());
    }

    @Test
    void testVerificarStatus_Marked() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(diarioFilmeRepository.findByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(Optional.of(diarioFilme));

        DiarioStatusResponse status = diarioFilmeService.verificarStatus(10L, "john_doe");

        assertNotNull(status);
        assertTrue(status.assistido());
        assertEquals("Great experience.", status.observacao());
    }

    @Test
    void testVerificarStatus_NotMarked() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(diarioFilmeRepository.findByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(Optional.empty());

        DiarioStatusResponse status = diarioFilmeService.verificarStatus(10L, "john_doe");

        assertNotNull(status);
        assertFalse(status.assistido());
        assertNull(status.observacao());
    }

    @Test
    void testVerificarStatus_UserNotFound() {
        when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            diarioFilmeService.verificarStatus(10L, "unknown");
        });

        assertEquals("Usuário não encontrado: unknown", exception.getMessage());
    }

    @Test
    void testObterEstatisticas_Success_WithAverageRating() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(diarioFilmeRepository.countByUsuarioId(1L)).thenReturn(15L);
        when(avaliacaoRepository.countByUsuarioId(1L)).thenReturn(8L);
        when(avaliacaoRepository.getAverageNotaByUsuarioId(1L)).thenReturn(4.34);

        DiarioEstatisticasResponse stats = diarioFilmeService.obterEstatisticas("john_doe");

        assertNotNull(stats);
        assertEquals(15L, stats.totalAssistidos());
        assertEquals(8L, stats.totalAvaliacoes());
        assertEquals(4.3, stats.notaMedia()); // 4.34 rounded to 1 decimal place
    }

    @Test
    void testObterEstatisticas_Success_WithoutAverageRating() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(diarioFilmeRepository.countByUsuarioId(1L)).thenReturn(0L);
        when(avaliacaoRepository.countByUsuarioId(1L)).thenReturn(0L);
        when(avaliacaoRepository.getAverageNotaByUsuarioId(1L)).thenReturn(null);

        DiarioEstatisticasResponse stats = diarioFilmeService.obterEstatisticas("john_doe");

        assertNotNull(stats);
        assertEquals(0L, stats.totalAssistidos());
        assertEquals(0.0, stats.notaMedia());
    }

    @Test
    void testObterEstatisticas_UserNotFound() {
        when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            diarioFilmeService.obterEstatisticas("unknown");
        });

        assertEquals("Usuário não encontrado: unknown", exception.getMessage());
    }
}
