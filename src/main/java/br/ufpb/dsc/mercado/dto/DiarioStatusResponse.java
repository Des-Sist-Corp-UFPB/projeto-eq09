package br.ufpb.dsc.mercado.dto;

import java.time.Instant;

public record DiarioStatusResponse(
    boolean assistido,
    Instant dataAssistido,
    String observacao
) {}
