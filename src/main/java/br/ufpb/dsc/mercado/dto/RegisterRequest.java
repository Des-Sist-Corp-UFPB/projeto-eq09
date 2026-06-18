package br.ufpb.dsc.mercado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "O nome de usuário é obrigatório")
    @Size(min = 3, max = 50, message = "O nome de usuário deve ter entre 3 e 50 caracteres")
    String username,

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, max = 255, message = "A senha deve ter pelo menos 6 caracteres")
    String password
) {}
