package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.Filme;
import br.ufpb.dsc.mercado.dto.FilmeRequest;
import br.ufpb.dsc.mercado.dto.FilmeResponse;
import br.ufpb.dsc.mercado.repository.AvaliacaoRepository;
import br.ufpb.dsc.mercado.repository.FilmeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmeService {

    private final FilmeRepository filmeRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    public FilmeService(FilmeRepository filmeRepository, AvaliacaoRepository avaliacaoRepository) {
        this.filmeRepository = filmeRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<FilmeResponse> listarTodos(String query) {
        List<Filme> filmes;
        if (query != null && !query.isBlank()) {
            filmes = filmeRepository.findByTituloContainingIgnoreCaseOrderByTituloAsc(query);
        } else {
            filmes = filmeRepository.findAllByOrderByTituloAsc();
        }

        return filmes.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FilmeResponse obterPorId(Long id) {
        Filme filme = filmeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado com o ID: " + id));
        return toResponse(filme);
    }

    @Transactional
    public FilmeResponse cadastrar(FilmeRequest request) {
        Filme filme = new Filme(
                request.titulo(),
                request.diretor(),
                request.ano(),
                request.sinopse(),
                request.genero(),
                request.imagemUrl()
        );

        Filme filmeSalvo = filmeRepository.save(filme);
        return toResponse(filmeSalvo);
    }

    private FilmeResponse toResponse(Filme filme) {
        Double notaMedia = avaliacaoRepository.getAverageNotaByFilmeId(filme.getId());
        long totalAvaliacoes = avaliacaoRepository.countByFilmeId(filme.getId());

        if (notaMedia != null) {
            notaMedia = Math.round(notaMedia * 10.0) / 10.0;
        } else {
            notaMedia = 0.0;
        }

        return new FilmeResponse(
                filme.getId(),
                filme.getTitulo(),
                filme.getDiretor(),
                filme.getAno(),
                filme.getSinopse(),
                filme.getGenero(),
                filme.getImagemUrl(),
                notaMedia,
                totalAvaliacoes,
                filme.getCriadoEm()
        );
    }
}
