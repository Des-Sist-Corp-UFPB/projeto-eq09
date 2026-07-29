package br.ufpb.dsc.mercado.dto;

import java.util.List;

public record ChatResponse(
    String resposta,
    List<FilmeRecomendacaoDTO> recomendacoes
) {}
