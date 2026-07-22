package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Avaliacao;
import br.ufpb.dsc.mercado.domain.Filme;
import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.dto.AvaliacaoRequest;
import br.ufpb.dsc.mercado.repository.AvaliacaoRepository;
import br.ufpb.dsc.mercado.repository.FilmeRepository;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final FilmeRepository filmeRepository;
    private final UsuarioRepository usuarioRepository;
    private final LogAuditoriaService logAuditoriaService;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, FilmeRepository filmeRepository,
                            UsuarioRepository usuarioRepository, LogAuditoriaService logAuditoriaService) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.filmeRepository = filmeRepository;
        this.usuarioRepository = usuarioRepository;
        this.logAuditoriaService = logAuditoriaService;
    }

    @Transactional
    public void avaliar(Long filmeId, String username, AvaliacaoRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + username));

        Filme filme = filmeRepository.findById(filmeId)
                .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + filmeId));

        // Busca se já existe uma avaliação desse usuário para este filme (se sim, atualiza; senão, cria uma nova)
        Avaliacao avaliacao = avaliacaoRepository.findByUsuarioIdAndFilmeId(usuario.getId(), filme.getId())
                .orElseGet(() -> new Avaliacao(usuario, filme, request.nota()));

        avaliacao.setNota(request.nota());
        avaliacaoRepository.save(avaliacao);
        
        logAuditoriaService.registrarLog(username, "AVALIAR_FILME", "Avaliou o filme: " + filme.getTitulo() + " (ID: " + filmeId + ") com nota: " + request.nota());
    }
}
