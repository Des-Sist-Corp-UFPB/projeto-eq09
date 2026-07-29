package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Comentario;
import br.ufpb.dsc.mercado.domain.Filme;
import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.dto.ComentarioRequest;
import br.ufpb.dsc.mercado.dto.ComentarioResponse;
import br.ufpb.dsc.mercado.repository.ComentarioRepository;
import br.ufpb.dsc.mercado.repository.FilmeRepository;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final FilmeRepository filmeRepository;
    private final UsuarioRepository usuarioRepository;
    private final LogAuditoriaService logAuditoriaService;

    public ComentarioService(ComentarioRepository comentarioRepository, FilmeRepository filmeRepository,
                             UsuarioRepository usuarioRepository, LogAuditoriaService logAuditoriaService) {
        this.comentarioRepository = comentarioRepository;
        this.filmeRepository = filmeRepository;
        this.usuarioRepository = usuarioRepository;
        this.logAuditoriaService = logAuditoriaService;
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listarPorFilme(Long filmeId) {
        return comentarioRepository.findByFilmeIdOrderByCriadoEmDesc(filmeId).stream()
                .map(c -> new ComentarioResponse(
                        c.getId(),
                        c.getTexto(),
                        c.getUsuario().getUsername(),
                        c.getCriadoEm()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public ComentarioResponse adicionar(Long filmeId, String username, ComentarioRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + username));

        Filme filme = filmeRepository.findById(filmeId)
                .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + filmeId));

        Comentario comentario = new Comentario(usuario, filme, request.texto());
        Comentario comentarioSalvo = comentarioRepository.save(comentario);

        logAuditoriaService.registrarLog(username, "COMENTAR_FILME", "Comentou no filme: " + filme.getTitulo() + " (ID: " + filmeId + ")");

        return new ComentarioResponse(
                comentarioSalvo.getId(),
                comentarioSalvo.getTexto(),
                comentarioSalvo.getUsuario().getUsername(),
                comentarioSalvo.getCriadoEm()
        );
    }

    @Transactional
    public void remover(Long filmeId, Long comentarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new IllegalArgumentException("Comentário não encontrado: " + comentarioId));

        if (!comentario.getFilme().getId().equals(filmeId)) {
            throw new IllegalArgumentException("Comentário não pertence ao filme informado.");
        }

        comentarioRepository.delete(comentario);
        
        logAuditoriaService.registrarLog("DELETAR_COMENTARIO", "Removeu o comentário (ID: " + comentarioId + ") do usuário: " + comentario.getUsuario().getUsername() + " no filme: " + comentario.getFilme().getTitulo() + " (ID: " + filmeId + ")");
    }
}
