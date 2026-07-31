package br.ufpb.dsc.mercado.dto;

public record FilmeRecomendacaoDTO(
    Long id,
    String titulo,
    String genero,
    String diretor,
    Integer ano,
    String imagemUrl,
    String motivo
) {}
