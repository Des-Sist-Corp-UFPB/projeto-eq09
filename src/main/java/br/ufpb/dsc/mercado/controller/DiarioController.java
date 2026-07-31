package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.dto.*;
import br.ufpb.dsc.mercado.service.DiarioFilmeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diario")
public class DiarioController {

    private final DiarioFilmeService diarioFilmeService;

    public DiarioController(DiarioFilmeService diarioFilmeService) {
        this.diarioFilmeService = diarioFilmeService;
    }

    @PostMapping("/filmes/{filmeId}")
    public ResponseEntity<String> marcarComoAssistido(@PathVariable Long filmeId,
                                                      @AuthenticationPrincipal UserDetails userDetails,
                                                      @RequestBody(required = false) DiarioRequest request) {
        DiarioRequest req = request != null ? request : new DiarioRequest("");
        try {
            diarioFilmeService.marcarComoAssistido(filmeId, userDetails.getUsername(), req);
            return ResponseEntity.ok("Filme marcado como assistido com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/filmes/{filmeId}")
    public ResponseEntity<String> removerMarcacao(@PathVariable Long filmeId,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        try {
            diarioFilmeService.removerMarcacao(filmeId, userDetails.getUsername());
            return ResponseEntity.ok("Marcação de assistido removida com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listarDiario(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<DiarioResponse> diario = diarioFilmeService.listarDiario(userDetails.getUsername());
            return ResponseEntity.ok(diario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/filmes/{filmeId}/verificar")
    public ResponseEntity<DiarioStatusResponse> verificarStatus(@PathVariable Long filmeId,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        try {
            DiarioStatusResponse status = diarioFilmeService.verificarStatus(filmeId, userDetails.getUsername());
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<DiarioEstatisticasResponse> obterEstatisticas(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            DiarioEstatisticasResponse estatisticas = diarioFilmeService.obterEstatisticas(userDetails.getUsername());
            return ResponseEntity.ok(estatisticas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
