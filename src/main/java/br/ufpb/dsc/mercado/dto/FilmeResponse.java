package br.ufpb.dsc.mercado.dto;

import java.time.Instant;

public record FilmeResponse(
    Long id,
    String titulo,
    String diretor,
    Integer ano,
    String sinopse,
    String genero,
    String imagemUrl,
    Double notaMedia,
    Long totalAvaliacoes,
    Instant criadoEm
) {}
