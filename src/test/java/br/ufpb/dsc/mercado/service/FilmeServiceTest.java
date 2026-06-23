package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Filme;
import br.ufpb.dsc.mercado.dto.FilmeRequest;
import br.ufpb.dsc.mercado.dto.FilmeResponse;
import br.ufpb.dsc.mercado.repository.AvaliacaoRepository;
import br.ufpb.dsc.mercado.repository.FilmeRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmeServiceTest {

    @Mock
    private FilmeRepository filmeRepository;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private LogAuditoriaService logAuditoriaService;

    @InjectMocks
    private FilmeService filmeService;

    private Filme filme;

    @BeforeEach
    void setUp() {
        filme = new Filme("Inception", "Christopher Nolan", 2010, "A thief steals corporate secrets...", "Sci-Fi", "http://inception.jpg");
        filme.setId(1L);
    }

    @Test
    void testListarTodos_WithoutQuery() {
        when(filmeRepository.findAllByOrderByTituloAsc()).thenReturn(List.of(filme));
        when(avaliacaoRepository.getAverageNotaByFilmeId(1L)).thenReturn(4.56);
        when(avaliacaoRepository.countByFilmeId(1L)).thenReturn(10L);

        List<FilmeResponse> result = filmeService.listarTodos(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Inception", result.get(0).titulo());
        assertEquals(4.6, result.get(0).notaMedia()); // 4.56 rounded to 1 decimal place
        assertEquals(10L, result.get(0).totalAvaliacoes());
        verify(filmeRepository, times(1)).findAllByOrderByTituloAsc();
    }

    @Test
    void testListarTodos_WithBlankQuery() {
        when(filmeRepository.findAllByOrderByTituloAsc()).thenReturn(List.of(filme));
        when(avaliacaoRepository.getAverageNotaByFilmeId(1L)).thenReturn(null);
        when(avaliacaoRepository.countByFilmeId(1L)).thenReturn(0L);

        List<FilmeResponse> result = filmeService.listarTodos("   ");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0.0, result.get(0).notaMedia());
        verify(filmeRepository, times(1)).findAllByOrderByTituloAsc();
    }

    @Test
    void testListarTodos_WithQuery() {
        when(filmeRepository.findByTituloContainingIgnoreCaseOrderByTituloAsc("Incept")).thenReturn(List.of(filme));
        when(avaliacaoRepository.getAverageNotaByFilmeId(1L)).thenReturn(4.0);
        when(avaliacaoRepository.countByFilmeId(1L)).thenReturn(1L);

        List<FilmeResponse> result = filmeService.listarTodos("Incept");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Inception", result.get(0).titulo());
        verify(filmeRepository, times(1)).findByTituloContainingIgnoreCaseOrderByTituloAsc("Incept");
    }

    @Test
    void testObterPorId_Success() {
        when(filmeRepository.findById(1L)).thenReturn(Optional.of(filme));
        when(avaliacaoRepository.getAverageNotaByFilmeId(1L)).thenReturn(4.2);
        when(avaliacaoRepository.countByFilmeId(1L)).thenReturn(5L);

        FilmeResponse response = filmeService.obterPorId(1L);

        assertNotNull(response);
        assertEquals("Inception", response.titulo());
        assertEquals(4.2, response.notaMedia());
        assertEquals(5L, response.totalAvaliacoes());
    }

    @Test
    void testObterPorId_NotFound() {
        when(filmeRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            filmeService.obterPorId(99L);
        });

        assertEquals("Filme não encontrado com o ID: 99", exception.getMessage());
    }

    @Test
    void testCadastrar_Success() {
        FilmeRequest request = new FilmeRequest("Interstellar", "Christopher Nolan", 2014, "Space travel...", "Sci-Fi", "http://interstellar.jpg");
        Filme savedFilme = new Filme("Interstellar", "Christopher Nolan", 2014, "Space travel...", "Sci-Fi", "http://interstellar.jpg");
        savedFilme.setId(2L);

        when(filmeRepository.save(any(Filme.class))).thenReturn(savedFilme);
        when(avaliacaoRepository.getAverageNotaByFilmeId(2L)).thenReturn(null);
        when(avaliacaoRepository.countByFilmeId(2L)).thenReturn(0L);

        FilmeResponse response = filmeService.cadastrar(request);

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals("Interstellar", response.titulo());
        assertEquals(0.0, response.notaMedia());
        
        verify(filmeRepository, times(1)).save(any(Filme.class));
        verify(logAuditoriaService, times(1)).registrarLog("CADASTRAR_FILME", "Filme cadastrado: Interstellar (ID: 2)");
    }

    @Test
    void testRemover_Success() {
        when(filmeRepository.findById(1L)).thenReturn(Optional.of(filme));

        filmeService.remover(1L);

        verify(filmeRepository, times(1)).delete(filme);
        verify(logAuditoriaService, times(1)).registrarLog("DELETAR_FILME", "Filme removido: Inception (ID: 1)");
    }

    @Test
    void testRemover_NotFound() {
        when(filmeRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            filmeService.remover(99L);
        });

        assertEquals("Filme não encontrado com o ID: 99", exception.getMessage());
        verify(filmeRepository, never()).delete(any(Filme.class));
    }
}
