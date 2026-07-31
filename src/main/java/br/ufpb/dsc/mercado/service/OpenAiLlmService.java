package br.ufpb.dsc.mercado.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiLlmService implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiLlmService.class);

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final RestClient restClient;

    public OpenAiLlmService(
            @Value("${openai.base-url:https://llm.rodrigor.com/v1}") String baseUrl,
            @Value("${openai.api-key:sk-bNVQrt-AKm1-5lOsI6fQ1g}") String apiKey,
            @Value("${openai.model:gpt-4o-mini}") String model,
            RestClient.Builder restClientBuilder) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = (restClientBuilder != null) ? restClientBuilder.build() : RestClient.builder().build();
    }

    @Override
    public String gerarResposta(String promptSistema, String promptUsuario) {
        return gerarRespostaComModelo(promptSistema, promptUsuario, this.model);
    }

    @Override
    public String gerarRespostaComModelo(String promptSistema, String promptUsuario, String modeloEspecifico) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("API Key para OpenAI/LiteLLM não configurada.");
            return null;
        }

        String targetModel = (modeloEspecifico != null && !modeloEspecifico.isBlank()) ? modeloEspecifico : this.model;
        String endpointUrl = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";

        Map<String, Object> requestBody = Map.of(
                "model", targetModel,
                "messages", List.of(
                        Map.of("role", "system", "content", promptSistema != null ? promptSistema : ""),
                        Map.of("role", "user", "content", promptUsuario != null ? promptUsuario : "")
                ),
                "temperature", 0.7
        );

        try {
            Map<?, ?> response = restClient.post()
                    .uri(endpointUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("choices")) {
                List<?> choices = (List<?>) response.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
                    if (message != null && message.containsKey("content")) {
                        return (String) message.get("content");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erro ao chamar o serviço LiteLLM/OpenAI (endpoint: {}): {}", endpointUrl, e.getMessage());
        }
        return null;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @Override
    public String getModel() {
        return model;
    }
}
