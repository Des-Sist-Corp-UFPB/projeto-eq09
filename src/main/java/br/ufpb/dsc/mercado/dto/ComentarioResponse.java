package br.ufpb.dsc.mercado.dto;

import java.time.Instant;

public record ComentarioResponse(
    Long id,
    String texto,
    String username,
    Instant criadoEm
) {}
