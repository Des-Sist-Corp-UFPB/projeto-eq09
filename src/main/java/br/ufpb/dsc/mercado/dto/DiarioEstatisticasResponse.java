package br.ufpb.dsc.mercado.dto;

public record DiarioEstatisticasResponse(
    long totalAssistidos,
    long totalAvaliacoes,
    Double notaMedia
) {}
