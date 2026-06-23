package br.ufpb.dsc.mercado.dto;

import java.time.Instant;

public record WatchlistStatusResponse(
    boolean naWatchlist,
    Instant dataAdicao
) {}
