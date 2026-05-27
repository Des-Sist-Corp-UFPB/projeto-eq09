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

    public ComentarioService(ComentarioRepository comentarioRepository, FilmeRepository filmeRepository,
                             UsuarioRepository usuarioRepository) {
        this.comentarioRepository = comentarioRepository;
        this.filmeRepository = filmeRepository;
        this.usuarioRepository = usuarioRepository;
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

        return new ComentarioResponse(
                comentarioSalvo.getId(),
                comentarioSalvo.getTexto(),
                comentarioSalvo.getUsuario().getUsername(),
                comentarioSalvo.getCriadoEm()
        );
    }
}
