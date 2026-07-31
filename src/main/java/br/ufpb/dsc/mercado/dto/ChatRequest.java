package br.ufpb.dsc.mercado.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
    @NotBlank(message = "A mensagem não pode estar vazia")
    String mensagem
) {}
