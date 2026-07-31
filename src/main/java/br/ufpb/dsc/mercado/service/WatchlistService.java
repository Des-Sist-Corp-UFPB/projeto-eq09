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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UsuarioRepository usuarioRepository;
    private final FilmeRepository filmeRepository;
    private final DiarioFilmeRepository diarioFilmeRepository;
    private final LogAuditoriaService logAuditoriaService;

    public WatchlistService(WatchlistRepository watchlistRepository,
                            UsuarioRepository usuarioRepository,
                            FilmeRepository filmeRepository,
                            DiarioFilmeRepository diarioFilmeRepository,
                            LogAuditoriaService logAuditoriaService) {
        this.watchlistRepository = watchlistRepository;
        this.usuarioRepository = usuarioRepository;
        this.filmeRepository = filmeRepository;
        this.diarioFilmeRepository = diarioFilmeRepository;
        this.logAuditoriaService = logAuditoriaService;
    }

    @Transactional
    public void adicionarFilme(Long filmeId, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + username));

        Filme filme = filmeRepository.findById(filmeId)
                .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + filmeId));

        if (watchlistRepository.existsByUsuarioIdAndFilmeId(usuario.getId(), filme.getId())) {
            return; // já está na watchlist
        }

        // Se o filme estiver marcado como assistido, remove do diário (Regra de exclusividade de estado)
        Optional<DiarioFilme> diarioOpt = diarioFilmeRepository.findByUsuarioIdAndFilmeId(usuario.getId(), filme.getId());
        if (diarioOpt.isPresent()) {
            diarioFilmeRepository.delete(diarioOpt.get());
            logAuditoriaService.registrarLog(username, "REMOVER_ASSISTIDO", "Removeu marcação de assistido para colocar na Watchlist: " + filme.getTitulo() + " (ID: " + filmeId + ")");
        }

        Watchlist watchlist = new Watchlist(usuario, filme);
        watchlistRepository.save(watchlist);

        logAuditoriaService.registrarLog(username, "ADICIONAR_WATCHLIST", "Adicionou o filme à watchlist: " + filme.getTitulo() + " (ID: " + filmeId + ")");
    }

    @Transactional
    public void removerFilme(Long filmeId, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + username));

        Filme filme = filmeRepository.findById(filmeId)
                .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + filmeId));

        Watchlist watchlist = watchlistRepository.findByUsuarioIdAndFilmeId(usuario.getId(), filme.getId())
                .orElseThrow(() -> new IllegalArgumentException("Filme não está na watchlist deste usuário."));

        watchlistRepository.delete(watchlist);
        logAuditoriaService.registrarLog(username, "REMOVER_WATCHLIST", "Removeu o filme da watchlist: " + filme.getTitulo() + " (ID: " + filmeId + ")");
    }

    @Transactional(readOnly = true)
    public List<WatchlistResponse> listarWatchlist(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + username));

        List<Watchlist> watchlistLista = watchlistRepository.findByUsuarioIdOrderByDataAdicaoDesc(usuario.getId());

        return watchlistLista.stream().map(w -> new WatchlistResponse(
                w.getId(),
                w.getFilme().getId(),
                w.getFilme().getTitulo(),
                w.getFilme().getImagemUrl(),
                w.getDataAdicao()
        )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WatchlistStatusResponse verificarStatus(Long filmeId, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + username));

        Optional<Watchlist> watchlistOpt = watchlistRepository.findByUsuarioIdAndFilmeId(usuario.getId(), filmeId);

        if (watchlistOpt.isPresent()) {
            return new WatchlistStatusResponse(true, watchlistOpt.get().getDataAdicao());
        }

        return new WatchlistStatusResponse(false, null);
    }

    @Transactional(readOnly = true)
    public long obterTotal(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + username));

        return watchlistRepository.countByUsuarioId(usuario.getId());
    }

    @Transactional(readOnly = true)
    public String obterUltimoFilmeAdicionado(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + username));

        Optional<Watchlist> ultimoOpt = watchlistRepository.findFirstByUsuarioIdOrderByDataAdicaoDesc(usuario.getId());

        return ultimoOpt.map(w -> w.getFilme().getTitulo()).orElse("-");
    }
}
