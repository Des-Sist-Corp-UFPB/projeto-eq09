package br.ufpb.dsc.mercado.dto;

public record SpoilerSegmentResponse(
    int start,
    int end,
    String level,
    String reason
) {}
