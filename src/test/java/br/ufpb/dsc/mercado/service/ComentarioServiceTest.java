package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Comentario;
import br.ufpb.dsc.mercado.domain.Filme;
import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.dto.ComentarioRequest;
import br.ufpb.dsc.mercado.dto.ComentarioResponse;
import br.ufpb.dsc.mercado.repository.ComentarioRepository;
import br.ufpb.dsc.mercado.repository.FilmeRepository;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
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
class ComentarioServiceTest {

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private FilmeRepository filmeRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LogAuditoriaService logAuditoriaService;

    @Mock
    private SpoilerDetectionService spoilerDetectionService;

    @Mock
    private AISpoilerService aiSpoilerService;

    @InjectMocks
    private ComentarioService comentarioService;

    private Usuario usuario;
    private Filme filme;
    private Comentario comentario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("john_doe", "pass", "USER");
        usuario.setId(1L);

        filme = new Filme("Inception", "Nolan", 2010, "Sci-Fi", "Action", "http://image.jpg");
        filme.setId(10L);

        comentario = new Comentario(usuario, filme, "Great movie!");
        comentario.setId(100L);
    }

    @Test
    void testListarPorFilme_Success() {
        when(comentarioRepository.findByFilmeIdOrderByCriadoEmDesc(10L)).thenReturn(List.of(comentario));

        List<ComentarioResponse> result = comentarioService.listarPorFilme(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Great movie!", result.get(0).texto());
        assertEquals("john_doe", result.get(0).username());
        verify(comentarioRepository, times(1)).findByFilmeIdOrderByCriadoEmDesc(10L);
    }

    @Test
    void testListarPorFilme_Empty() {
        when(comentarioRepository.findByFilmeIdOrderByCriadoEmDesc(10L)).thenReturn(Collections.emptyList());

        List<ComentarioResponse> result = comentarioService.listarPorFilme(10L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAdicionar_Success() {
        ComentarioRequest request = new ComentarioRequest("Cool film");
        Comentario savedComentario = new Comentario(usuario, filme, "Cool film");
        savedComentario.setId(101L);

        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(10L)).thenReturn(Optional.of(filme));
        when(comentarioRepository.save(any(Comentario.class))).thenReturn(savedComentario);

        ComentarioResponse response = comentarioService.adicionar(10L, "john_doe", request);

        assertNotNull(response);
        assertEquals(101L, response.id());
        assertEquals("Cool film", response.texto());
        assertEquals("john_doe", response.username());

        verify(comentarioRepository, times(1)).save(any(Comentario.class));
        verify(logAuditoriaService, times(1)).registrarLog("john_doe", "COMENTAR_FILME", "Comentou no filme: Inception (ID: 10)");
    }

    @Test
    void testAdicionar_UserNotFound() {
        ComentarioRequest request = new ComentarioRequest("Cool film");

        when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            comentarioService.adicionar(10L, "unknown", request);
        });

        assertEquals("Usuário não encontrado: unknown", exception.getMessage());
        verify(comentarioRepository, never()).save(any(Comentario.class));
    }

    @Test
    void testAdicionar_FilmNotFound() {
        ComentarioRequest request = new ComentarioRequest("Cool film");

        when(usuarioRepository.findByUsername("john_doe")).thenReturn(Optional.of(usuario));
        when(filmeRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            comentarioService.adicionar(99L, "john_doe", request);
        });

        assertEquals("Filme não encontrado: 99", exception.getMessage());
        verify(comentarioRepository, never()).save(any(Comentario.class));
    }

    @Test
    void testRemover_Success() {
        when(comentarioRepository.findById(100L)).thenReturn(Optional.of(comentario));

        comentarioService.remover(10L, 100L);

        verify(comentarioRepository, times(1)).delete(comentario);
        verify(logAuditoriaService, times(1)).registrarLog("DELETAR_COMENTARIO", "Removeu o comentário (ID: 100) do usuário: john_doe no filme: Inception (ID: 10)");
    }

    @Test
    void testRemover_CommentNotFound() {
        when(comentarioRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            comentarioService.remover(10L, 999L);
        });

        assertEquals("Comentário não encontrado: 999", exception.getMessage());
        verify(comentarioRepository, never()).delete(any(Comentario.class));
    }

    @Test
    void testRemover_CommentFilmIdMismatch() {
        when(comentarioRepository.findById(100L)).thenReturn(Optional.of(comentario));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            comentarioService.remover(99L, 100L); // 99 mismatch with 10L
        });

        assertEquals("Comentário não pertence ao filme informado.", exception.getMessage());
        verify(comentarioRepository, never()).delete(any(Comentario.class));
    }
}
