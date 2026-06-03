package br.ufpb.dsc.mercado.dto;

public record PingResponse(
    String status,
    String service,
    String timestamp
) {}
