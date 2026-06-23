package br.ufpb.dsc.mercado.dto;

import java.time.Instant;

public record DiarioResponse(
    Long id,
    Long filmeId,
    String titulo,
    String imagemUrl,
    Instant dataAssistido,
    String observacao,
    Integer notaAvaliacao
) {}
