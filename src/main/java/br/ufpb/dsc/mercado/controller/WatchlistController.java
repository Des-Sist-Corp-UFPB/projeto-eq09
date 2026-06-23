package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.dto.*;
import br.ufpb.dsc.mercado.service.WatchlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @PostMapping("/filmes/{filmeId}")
    public ResponseEntity<String> adicionarFilme(@PathVariable Long filmeId,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        try {
            watchlistService.adicionarFilme(filmeId, userDetails.getUsername());
            return ResponseEntity.ok("Filme adicionado à watchlist com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/filmes/{filmeId}")
    public ResponseEntity<String> removerFilme(@PathVariable Long filmeId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            watchlistService.removerFilme(filmeId, userDetails.getUsername());
            return ResponseEntity.ok("Filme removido da watchlist com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listarWatchlist(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<WatchlistResponse> watchlist = watchlistService.listarWatchlist(userDetails.getUsername());
            return ResponseEntity.ok(watchlist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/filmes/{filmeId}/verificar")
    public ResponseEntity<WatchlistStatusResponse> verificarStatus(@PathVariable Long filmeId,
                                                                   @AuthenticationPrincipal UserDetails userDetails) {
        try {
            WatchlistStatusResponse status = watchlistService.verificarStatus(filmeId, userDetails.getUsername());
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/total")
    public ResponseEntity<?> obterTotal(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            long total = watchlistService.obterTotal(userDetails.getUsername());
            String ultimoAdicionado = watchlistService.obterUltimoFilmeAdicionado(userDetails.getUsername());
            return ResponseEntity.ok(new WatchlistStatsDto(total, ultimoAdicionado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Record interno simples para retornar dados agregados da watchlist
    public record WatchlistStatsDto(long total, String ultimoAdicionado) {}
}
