package br.ufpb.dsc.mercado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FilmeRequest(
    @NotBlank(message = "O título do filme é obrigatório")
    @Size(max = 150)
    String titulo,

    @Size(max = 100)
    String diretor,

    @NotNull(message = "O ano de lançamento é obrigatório")
    Integer ano,

    String sinopse,

    @Size(max = 50)
    String genero,

    @Size(max = 500)
    String imagemUrl
) {}
