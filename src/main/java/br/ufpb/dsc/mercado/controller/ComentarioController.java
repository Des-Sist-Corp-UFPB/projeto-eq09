package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.dto.ComentarioRequest;
import br.ufpb.dsc.mercado.dto.ComentarioResponse;
import br.ufpb.dsc.mercado.service.ComentarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/filmes/{filmeId}/comentarios")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping
    public ResponseEntity<List<ComentarioResponse>> listarPorFilme(@PathVariable Long filmeId) {
        List<ComentarioResponse> comentarios = comentarioService.listarPorFilme(filmeId);
        return ResponseEntity.ok(comentarios);
    }

    @PostMapping
    public ResponseEntity<?> adicionar(@PathVariable Long filmeId,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       @Valid @RequestBody ComentarioRequest request) {
        try {
            ComentarioResponse comentario = comentarioService.adicionar(filmeId, userDetails.getUsername(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(comentario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{comentarioId}")
    public ResponseEntity<?> remover(@PathVariable Long filmeId, @PathVariable Long comentarioId) {
        try {
            comentarioService.remover(filmeId, comentarioId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
