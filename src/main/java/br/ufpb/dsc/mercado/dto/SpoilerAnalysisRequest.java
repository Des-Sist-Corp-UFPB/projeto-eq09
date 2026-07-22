package br.ufpb.dsc.mercado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SpoilerAnalysisRequest(
    @NotNull(message = "O ID do filme é obrigatório")
    Long movieId,

    String movieTitle,

    @NotBlank(message = "A review não pode estar vazia")
    String review
) {}
