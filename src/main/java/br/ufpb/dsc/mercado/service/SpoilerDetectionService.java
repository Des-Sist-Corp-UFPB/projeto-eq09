package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.dto.SpoilerAnalysisRequest;
import br.ufpb.dsc.mercado.dto.SpoilerAnalysisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SpoilerDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(SpoilerDetectionService.class);
    private final AISpoilerService aiSpoilerService;

    public SpoilerDetectionService(AISpoilerService aiSpoilerService) {
        this.aiSpoilerService = aiSpoilerService;
    }

    /**
     * Analisa o comentário utilizando a IA.
     * 
     * Retorna se é spoiler ou não. Em caso de falha/timeout, trata graciosamente 
     * e retorna false (não spoiler) registrando no log.
     * Aplica o critério de confiança: se a confiança for inferior a 0.70 (70%), considera false.
     */
    public boolean isSpoiler(String textoComentario, Long filmeId, String filmeTitulo) {
        if (textoComentario == null || textoComentario.isBlank()) {
            return false;
        }

        try {
            SpoilerAnalysisRequest request = new SpoilerAnalysisRequest(filmeId, filmeTitulo, textoComentario);
            SpoilerAnalysisResponse response = aiSpoilerService.analyzeReview(request);

            if (response != null && response.containsSpoiler() && response.confidence() >= 0.70) {
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Erro ou timeout ao consultar serviço de IA de detecção de spoiler. Comentário salvo como não spoiler. Erro: {}", e.getMessage());
            return false;
        }
    }
}
