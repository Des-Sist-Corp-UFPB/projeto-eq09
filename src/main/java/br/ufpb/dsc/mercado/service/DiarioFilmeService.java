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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DiarioFilmeService {

    private final DiarioFilmeRepository diarioFilmeRepository;
    private final UsuarioRepository usuarioRepository;
    private final FilmeRepository filmeRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final WatchlistRepository watchlistRepository;
    private final LogAuditoriaService logAuditoriaService;

    public DiarioFilmeService(DiarioFilmeRepository diarioFilmeRepository,
                              UsuarioRepository usuarioRepository,
                              FilmeRepository filmeRepository,
                              AvaliacaoRepository avaliacaoRepository,
                              WatchlistRepository watchlistRepository,
                              LogAuditoriaService logAuditoriaService) {
        this.diarioFilmeRepository = diarioFilmeRepository;
        this.usuarioRepository = usuarioRepository;
        this.filmeRepository = filmeRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.watchlistRepository = watchlistRepository;
        this.logAuditoriaService = logAuditoriaService;
    }

    @Transactional
    public void marcarComoAssistido(Long filmeId, String username, DiarioRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + username));

        Filme filme = filmeRepository.findById(filmeId)
                .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + filmeId));

        // Remove da watchlist se existir (Regra de exclusividade de estado)
        watchlistRepository.deleteByUsuarioIdAndFilmeId(usuario.getId(), filme.getId());

        Optional<DiarioFilme> diarioOpt = diarioFilmeRepository.findByUsuarioIdAndFilmeId(usuario.getId(), filme.getId());

        if (diarioOpt.isPresent()) {
            DiarioFilme diario = diarioOpt.get();
            diario.setObservacao(request.observacao());
            diarioFilmeRepository.save(diario);
        } else {
            DiarioFilme diario = new DiarioFilme(usuario, filme, request.observacao());
            diarioFilmeRepository.save(diario);
        }

        logAuditoriaService.registrarLog(username, "MARCAR_ASSISTIDO", "Marcou o filme como assistido: " + filme.getTitulo() + " (ID: " + filmeId + ")");
    }

    @Transactional
    public void removerMarcacao(Long filmeId, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + username));

        Filme filme = filmeRepository.findById(filmeId)
                .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + filmeId));

        DiarioFilme diario = diarioFilmeRepository.findByUsuarioIdAndFilmeId(usuario.getId(), filme.getId())
                .orElseThrow(() -> new IllegalArgumentException("Filme não está marcado como assistido por este usuário."));

        diarioFilmeRepository.delete(diario);
        logAuditoriaService.registrarLog(username, "REMOVER_ASSISTIDO", "Removeu marcação de assistido do filme: " + filme.getTitulo() + " (ID: " + filmeId + ")");
    }

    @Transactional(readOnly = true)
    public List<DiarioResponse> listarDiario(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + username));

        List<DiarioFilme> diarioLista = diarioFilmeRepository.findByUsuarioIdOrderByDataAssistidoDesc(usuario.getId());

        return diarioLista.stream().map(diario -> {
            Optional<Avaliacao> avaliacaoOpt = avaliacaoRepository.findByUsuarioIdAndFilmeId(usuario.getId(), diario.getFilme().getId());
            Integer nota = avaliacaoOpt.map(Avaliacao::getNota).orElse(null);

            return new DiarioResponse(
                    diario.getId(),
                    diario.getFilme().getId(),
                    diario.getFilme().getTitulo(),
                    diario.getFilme().getImagemUrl(),
                    diario.getDataAssistido(),
                    diario.getObservacao(),
                    nota
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DiarioStatusResponse verificarStatus(Long filmeId, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + username));

        Optional<DiarioFilme> diarioOpt = diarioFilmeRepository.findByUsuarioIdAndFilmeId(usuario.getId(), filmeId);

        if (diarioOpt.isPresent()) {
            DiarioFilme diario = diarioOpt.get();
            return new DiarioStatusResponse(true, diario.getDataAssistido(), diario.getObservacao());
        }

        return new DiarioStatusResponse(false, null, null);
    }

    @Transactional(readOnly = true)
    public DiarioEstatisticasResponse obterEstatisticas(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + username));

        long totalAssistidos = diarioFilmeRepository.countByUsuarioId(usuario.getId());
        long totalAvaliacoes = avaliacaoRepository.countByUsuarioId(usuario.getId());
        Double notaMedia = avaliacaoRepository.getAverageNotaByUsuarioId(usuario.getId());

        if (notaMedia != null) {
            notaMedia = Math.round(notaMedia * 10.0) / 10.0;
        } else {
            notaMedia = 0.0;
        }

        return new DiarioEstatisticasResponse(totalAssistidos, totalAvaliacoes, notaMedia);
    }
}
