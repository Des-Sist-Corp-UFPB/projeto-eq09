package br.ufpb.dsc.mercado.dto;

public record AuthResponse(
    String token,
    String username,
    String role
) {}
