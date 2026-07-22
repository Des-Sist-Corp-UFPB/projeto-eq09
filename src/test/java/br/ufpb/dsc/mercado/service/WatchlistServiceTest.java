package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.DiarioFilme;
import br.ufpb.dsc.mercado.domain.Filme;
import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.domain.Watchlist;
import br.ufpb.dsc.mercado.dto.WatchlistResponse;
import br.ufpb.dsc.mercado.dto.WatchlistStatusResponse;
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
class WatchlistServiceTest {

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private FilmeRepository filmeRepository;

    @Mock
    private DiarioFilmeRepository diarioFilmeRepository;

    @Mock
    private LogAuditoriaService logAuditoriaService;

    @InjectMocks
    private WatchlistService watchlistService;

    private Usuario usuario;
    private Filme filme;
    private Watchlist watchlist;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("john_doe", "pass", "USER");
        usuario.setId(1L);

        filme = new Filme("Inception", "Nolan", 2010, "Sci-Fi", "Action", "http://image.jpg");
        filme.setId(10L);

        watchlist = new Watchlist(usuario, filme);
        watchlist.setId(100L);
    }

    @Test
    void testAdicionarFilme_AlreadyInWatchlist_ReturnsEarly() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme));
        when(watchlistRepository.existsByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(true);

        watchlistService.adicionarFilme(10L, "john_doe");

        verify(watchlistRepository, never()).save(any(Watchlist.class));
        verify(diarioFilmeRepository, never()).findByUsuarioIdAndFilmeId(anyLong(), anyLong());
    }

    @Test
    void testAdicionarFilme_New_NotInDiary() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme));
        when(watchlistRepository.existsByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(false);
        when(diarioFilmeRepository.findByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(Optional.empty());

        watchlistService.adicionarFilme(10L, "john_doe");

        verify(watchlistRepository, times(1)).save(any(Watchlist.class));
        verify(logAuditoriaService, times(1)).registrarLog("john_doe", "ADICIONAR_WATCHLIST", "Adicionou o filme à watchlist: Inception (ID: 10)");
    }

    @Test
    void testAdicionarFilme_New_InDiary() {
        DiarioFilme diario = new DiarioFilme(usuario, filme, "Notes");
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme));
        when(watchlistRepository.existsByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(false);
        when(diarioFilmeRepository.findByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(Optional.of(diario));

        watchlistService.adicionarFilme(10L, "john_doe");

        verify(diarioFilmeRepository, times(1)).delete(diario);
        verify(watchlistRepository, times(1)).save(any(Watchlist.class));
        verify(logAuditoriaService, times(1)).registrarLog("john_doe", "REMOVER_ASSISTIDO", "Removeu marcação de assistido para colocar na Watchlist: Inception (ID: 10)");
        verify(logAuditoriaService, times(1)).registrarLog("john_doe", "ADICIONAR_WATCHLIST", "Adicionou o filme à watchlist: Inception (ID: 10)");
    }

    @Test
    void testAdicionarFilme_UserNotFound() {
        when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            watchlistService.adicionarFilme(10L, "unknown");
        });

        assertEquals("Usuário não encontrado: unknown", exception.getMessage());
    }

    @Test
    void testAdicionarFilme_FilmNotFound() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            watchlistService.adicionarFilme(99L, "john_doe");
        });

        assertEquals("Filme não encontrado: 99", exception.getMessage());
    }

    @Test
    void testRemoverFilme_Success() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme));
        when(watchlistRepository.findByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(Optional.of(watchlist));

        watchlistService.removerFilme(10L, "john_doe");

        verify(watchlistRepository, times(1)).delete(watchlist);
        verify(logAuditoriaService, times(1)).registrarLog("john_doe", "REMOVER_WATCHLIST", "Removeu o filme da watchlist: Inception (ID: 10)");
    }

    @Test
    void testRemoverFilme_UserNotFound() {
        when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            watchlistService.removerFilme(10L, "unknown");
        });

        assertEquals("Usuário não encontrado: unknown", exception.getMessage());
    }

    @Test
    void testRemoverFilme_FilmNotFound() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            watchlistService.removerFilme(99L, "john_doe");
        });

        assertEquals("Filme não encontrado: 99", exception.getMessage());
    }

    @Test
    void testRemoverFilme_WatchlistEntryNotFound() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme));
        when(watchlistRepository.findByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            watchlistService.removerFilme(10L, "john_doe");
        });

        assertEquals("Filme não está na watchlist deste usuário.", exception.getMessage());
    }

    @Test
    void testListarWatchlist_Success() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(watchlistRepository.findByUsuarioIdOrderByDataAdicaoDesc(1L)).thenReturn(List.of(watchlist));

        List<WatchlistResponse> result = watchlistService.listarWatchlist("john_doe");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).filmeId());
        assertEquals("Inception", result.get(0).titulo());
    }

    @Test
    void testListarWatchlist_UserNotFound() {
        when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            watchlistService.listarWatchlist("unknown");
        });

        assertEquals("Usuário não encontrado: unknown", exception.getMessage());
    }

    @Test
    void testVerificarStatus_InWatchlist() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(watchlistRepository.findByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(Optional.of(watchlist));

        WatchlistStatusResponse status = watchlistService.verificarStatus(10L, "john_doe");

        assertNotNull(status);
        assertTrue(status.naWatchlist());
    }

    @Test
    void testVerificarStatus_NotInWatchlist() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(watchlistRepository.findByUsuarioIdAndFilmeId(1L, 10L)).thenReturn(Optional.empty());

        WatchlistStatusResponse status = watchlistService.verificarStatus(10L, "john_doe");

        assertNotNull(status);
        assertFalse(status.naWatchlist());
    }

    @Test
    void testVerificarStatus_UserNotFound() {
        when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            watchlistService.verificarStatus(10L, "unknown");
        });

        assertEquals("Usuário não encontrado: unknown", exception.getMessage());
    }

    @Test
    void testObterTotal_Success() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(watchlistRepository.countByUsuarioId(1L)).thenReturn(10L);

        long total = watchlistService.obterTotal("john_doe");

        assertEquals(10L, total);
    }

    @Test
    void testObterTotal_UserNotFound() {
        when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            watchlistService.obterTotal("unknown");
        });

        assertEquals("Usuário não encontrado: unknown", exception.getMessage());
    }

    @Test
    void testObterUltimoFilmeAdicionado_WithEntries() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(watchlistRepository.findFirstByUsuarioIdOrderByDataAdicaoDesc(1L)).thenReturn(Optional.of(watchlist));

        String title = watchlistService.obterUltimoFilmeAdicionado("john_doe");

        assertEquals("Inception", title);
    }

    @Test
    void testObterUltimoFilmeAdicionado_NoEntries() {
        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(watchlistRepository.findFirstByUsuarioIdOrderByDataAdicaoDesc(1L)).thenReturn(Optional.empty());

        String title = watchlistService.obterUltimoFilmeAdicionado("john_doe");

        assertEquals("-", title);
    }

    @Test
    void testObterUltimoFilmeAdicionado_UserNotFound() {
        when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            watchlistService.obterUltimoFilmeAdicionado("unknown");
        });

        assertEquals("Usuário não encontrado: unknown", exception.getMessage());
    }
}
