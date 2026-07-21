package br.ufpb.dsc.mercado.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.ufpb.dsc.mercado.dto.SpoilerAnalysisRequest;
import br.ufpb.dsc.mercado.dto.SpoilerAnalysisResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpoilerDetectionServiceTest {

    @Mock
    private AISpoilerService aiSpoilerService;

    @InjectMocks
    private SpoilerDetectionService spoilerDetectionService;

    @Test
    void testIsSpoiler_TrueWhenHighConfidence() {
        SpoilerAnalysisResponse response = new SpoilerAnalysisResponse(true, 0.85, "high", java.util.List.of());
        when(aiSpoilerService.analyzeReview(any(SpoilerAnalysisRequest.class))).thenReturn(response);

        boolean result = spoilerDetectionService.isSpoiler("No final ele morre.", 1L, "Filme X");

        assertTrue(result);
    }

    @Test
    void testIsSpoiler_FalseWhenLowConfidence() {
        // Confiança < 0.70 deve ser considerado não spoiler
        SpoilerAnalysisResponse response = new SpoilerAnalysisResponse(true, 0.65, "low", java.util.List.of());
        when(aiSpoilerService.analyzeReview(any(SpoilerAnalysisRequest.class))).thenReturn(response);

        boolean result = spoilerDetectionService.isSpoiler("Talvez aconteça algo.", 1L, "Filme X");

        assertFalse(result);
    }

    @Test
    void testIsSpoiler_FalseOnException() {
        when(aiSpoilerService.analyzeReview(any(SpoilerAnalysisRequest.class))).thenThrow(new RuntimeException("Timeout ou erro de API"));

        boolean result = spoilerDetectionService.isSpoiler("No final ele morre.", 1L, "Filme X");

        assertFalse(result);
    }
}
