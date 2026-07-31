package br.ufpb.dsc.mercado.service;

public interface LlmService {
    String gerarResposta(String promptSistema, String promptUsuario);
    String gerarRespostaComModelo(String promptSistema, String promptUsuario, String modeloEspecifico);
    String getBaseUrl();
    String getApiKey();
    String getModel();
}
