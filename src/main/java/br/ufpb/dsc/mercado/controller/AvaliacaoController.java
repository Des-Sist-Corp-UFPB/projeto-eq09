package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.dto.AvaliacaoRequest;
import br.ufpb.dsc.mercado.service.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/filmes/{filmeId}/avaliar")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping
    public ResponseEntity<String> avaliar(@PathVariable Long filmeId,
                                          @AuthenticationPrincipal UserDetails userDetails,
                                          @Valid @RequestBody AvaliacaoRequest request) {
        try {
            avaliacaoService.avaliar(filmeId, userDetails.getUsername(), request);
            return ResponseEntity.ok("Filme avaliado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
