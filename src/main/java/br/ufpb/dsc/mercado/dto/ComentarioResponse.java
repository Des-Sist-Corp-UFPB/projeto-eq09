package br.ufpb.dsc.mercado.dto;

import java.time.Instant;

import java.util.List;

public record ComentarioResponse(
    Long id,
    String texto,
    String username,
    Instant criadoEm,
    Boolean containsSpoiler,
    Double spoilerConfidence,
    String spoilerLevel,
    List<SpoilerSegmentResponse> spoilerSegments
) {
    public ComentarioResponse(Long id, String texto, String username, Instant criadoEm) {
        this(id, texto, username, criadoEm, false, 0.0, "none", List.of());
    }

    public Boolean isSpoiler() {
        return Boolean.TRUE.equals(containsSpoiler);
    }
}
