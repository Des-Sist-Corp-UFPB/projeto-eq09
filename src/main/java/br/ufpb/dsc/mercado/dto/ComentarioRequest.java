package br.ufpb.dsc.mercado.dto;

import jakarta.validation.constraints.NotBlank;

public record ComentarioRequest(
    @NotBlank(message = "O texto do comentário não pode estar vazio")
    String texto
) {}
