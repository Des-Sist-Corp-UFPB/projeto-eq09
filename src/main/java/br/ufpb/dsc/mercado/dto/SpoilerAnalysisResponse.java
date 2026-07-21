package br.ufpb.dsc.mercado.dto;

import java.util.List;

public record SpoilerAnalysisResponse(
    boolean containsSpoiler,
    double confidence,
    String level,
    List<SpoilerSegmentResponse> segments
) {}
