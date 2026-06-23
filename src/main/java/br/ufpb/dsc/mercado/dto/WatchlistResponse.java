package br.ufpb.dsc.mercado.dto;

import java.time.Instant;

public record WatchlistResponse(
    Long id,
    Long filmeId,
    String titulo,
    String imagemUrl,
    Instant dataAdicao
) {}
