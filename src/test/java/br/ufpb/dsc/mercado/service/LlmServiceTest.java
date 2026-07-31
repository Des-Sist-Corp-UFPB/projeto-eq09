package br.ufpb.dsc.mercado.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

class LlmServiceTest {

    private LlmService llmService;

    @BeforeEach
    void setUp() {
        llmService = new OpenAiLlmService(
                "https://llm.rodrigor.com/v1",
                "sk-bNVQrt-AKm1-5lOsI6fQ1g",
                "gpt-4o-mini",
                RestClient.builder()
        );
    }

    @Test
    void testConfigurationGetters() {
        assertEquals("https://llm.rodrigor.com/v1", llmService.getBaseUrl());
        assertEquals("sk-bNVQrt-AKm1-5lOsI6fQ1g", llmService.getApiKey());
        assertEquals("gpt-4o-mini", llmService.getModel());
    }

    @Test
    void testGerarResposta_MissingApiKey() {
        LlmService emptyKeyService = new OpenAiLlmService(
                "https://llm.rodrigor.com/v1",
                "",
                "gpt-4o-mini",
                RestClient.builder()
        );
        String result = emptyKeyService.gerarResposta("System prompt", "User prompt");
        assertNull(result);
    }
}
