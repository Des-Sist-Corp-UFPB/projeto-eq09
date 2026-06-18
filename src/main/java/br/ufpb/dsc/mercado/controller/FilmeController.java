package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.dto.FilmeRequest;
import br.ufpb.dsc.mercado.dto.FilmeResponse;
import br.ufpb.dsc.mercado.service.FilmeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/filmes")
public class FilmeController {

    private final FilmeService filmeService;

    public FilmeController(FilmeService filmeService) {
        this.filmeService = filmeService;
    }

    @GetMapping
    public ResponseEntity<List<FilmeResponse>> listarTodos(@RequestParam(required = false) String busca) {
        List<FilmeResponse> filmes = filmeService.listarTodos(busca);
        return ResponseEntity.ok(filmes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmeResponse> obterPorId(@PathVariable Long id) {
        try {
            FilmeResponse filme = filmeService.obterPorId(id);
            return ResponseEntity.ok(filme);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    public ResponseEntity<FilmeResponse> cadastrar(@Valid @RequestBody FilmeRequest request) {
        FilmeResponse filme = filmeService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(filme);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        try {
            filmeService.remover(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
