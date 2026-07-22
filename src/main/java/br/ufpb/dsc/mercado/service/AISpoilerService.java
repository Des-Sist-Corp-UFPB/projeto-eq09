package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.dto.SpoilerAnalysisRequest;
import br.ufpb.dsc.mercado.dto.SpoilerAnalysisResponse;
import br.ufpb.dsc.mercado.dto.SpoilerSegmentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AISpoilerService {

    public static final String MODEL_VERSION = "ai-spoiler-detector-v1.0";

    // Padrões explícitos e semânticos em Português e Inglês
    private static final List<SpoilerPatternRule> RULES = List.of(
        // Desfecho e Sacrifício
        new SpoilerPatternRule(
            Pattern.compile("(?i)\\b(no final|na última cena|no desfecho|nos momentos finais|at the end|in the final scene|final twist)\\b.*\\b(morre|morrem|se sacrifica|sacrifício|dies|is killed|sacrifices|dies in the end|suicide)\\b"),
            "high",
            "Revela o desfecho dramático e o destino/morte do personagem."
        ),
        new SpoilerPatternRule(
            Pattern.compile("(?i)\\b(descobrimos que|a revelação é|o plot twist|na verdade|plot twist is|turns out that|the revelation is|we find out that|revealed that)\\b.*\\b(assassino|vilão|vilã|fantasma|irmão|mesma pessoa|clonado|morto|killer|villain|ghost|dead all along|same person)\\b"),
            "high",
            "Revela reviravolta narrativa crucial ou identidade secreta."
        ),
        new SpoilerPatternRule(
            Pattern.compile("(?i)\\b(quem morre é|ele morre|ela morre|o protagonista morre|who dies is|he dies|she dies|main character dies)\\b"),
            "high",
            "Revela a morte de um personagem principal."
        ),
        new SpoilerPatternRule(
            Pattern.compile("(?i)\\b(o verdadeiro vilão|o vilão é|the real villain|the villain is)\\b"),
            "high",
            "Revela a verdadeira identidade do vilão."
        ),
        new SpoilerPatternRule(
            Pattern.compile("(?i)\\b(o final surpreende quando|no clímax|na cena pós-créditos|in the post-credits|in the climax)\\b"),
            "medium",
            "Descreve trechos de alta relevância sobre o clímax do filme."
        ),
        new SpoilerPatternRule(
            Pattern.compile("(?i)\\b(segredo guardado|segredo revelado|secret revealed)\\b"),
            "low",
            "Descreve revelação de segredo secundário da trama."
        )
    );

    // Expressões puramente técnicas ou opiniões gerais que NUNCA são spoilers
    private static final List<Pattern> TECHNICAL_PATTERNS = List.of(
        Pattern.compile("(?i)^[\\s\\S]*\\b(gostei do filme|ótima fotografia|excelente trilha sonora|belas atuações|ótima direção|recomendo assistir|filme emocionante|muito divertido|cinematografia incrível|ótima edição|great acting|amazing cinematography|loved the movie|great soundtrack|excellent direction|highly recommend)\\b[\\s\\S]*$")
    );

    private final Map<String, SpoilerAnalysisResponse> cacheMap = new ConcurrentHashMap<>();

    public SpoilerAnalysisResponse analyzeReview(SpoilerAnalysisRequest request) {
        if (request == null || request.review() == null || request.review().isBlank()) {
            return new SpoilerAnalysisResponse(false, 0.0, "none", Collections.emptyList());
        }

        String review = request.review();
        String cacheKey = (request.movieId() != null ? request.movieId() : "") + ":" + review.trim();
        if (cacheMap.containsKey(cacheKey)) {
            return cacheMap.get(cacheKey);
        }

        List<SpoilerSegmentResponse> segments = new ArrayList<>();
        double maxConfidence = 0.0;
        String highestLevel = "low";

        // Divisão do texto por frases/linhas mantendo posições exatas
        List<TextSegment> textSegments = extractSentences(review);

        for (TextSegment seg : textSegments) {
            String text = seg.text;
            if (isPureTechnicalOrOpinion(text)) {
                continue;
            }

            SpoilerAnalysisResult match = evaluateSegment(text, request.movieTitle());
            if (match.containsSpoiler) {
                segments.add(new SpoilerSegmentResponse(seg.start, seg.end, match.level, match.reason));
                if (match.confidence > maxConfidence) {
                    maxConfidence = match.confidence;
                }
                highestLevel = elevateLevel(highestLevel, match.level);
            }
        }

        boolean containsSpoiler = !segments.isEmpty() && maxConfidence >= 0.60;
        SpoilerAnalysisResponse response = new SpoilerAnalysisResponse(
            containsSpoiler,
            containsSpoiler ? Math.round(maxConfidence * 100.0) / 100.0 : 0.0,
            containsSpoiler ? highestLevel : "none",
            containsSpoiler ? segments : Collections.emptyList()
        );

        cacheMap.put(cacheKey, response);
        return response;
    }

    private boolean isPureTechnicalOrOpinion(String text) {
        String lower = text.trim().toLowerCase();
        for (Pattern pattern : TECHNICAL_PATTERNS) {
            if (pattern.matcher(lower).matches()) {
                if (!containsPlotKeywords(lower)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsPlotKeywords(String text) {
        return text.contains("morre") || text.contains("assassino") || text.contains("vilão") 
            || text.contains("final") || text.contains("sacrifica") || text.contains("dies") || text.contains("killer");
    }

    private SpoilerAnalysisResult evaluateSegment(String text, String movieTitle) {
        for (SpoilerPatternRule rule : RULES) {
            Matcher m = rule.pattern.matcher(text);
            if (m.find()) {
                double confidence = rule.level.equals("high") ? 0.94 : (rule.level.equals("medium") ? 0.82 : 0.68);
                if (movieTitle != null && !movieTitle.isBlank() && text.toLowerCase().contains(movieTitle.toLowerCase())) {
                    confidence = Math.min(0.99, confidence + 0.05);
                }
                return new SpoilerAnalysisResult(true, confidence, rule.level, rule.reason);
            }
        }

        String lower = text.toLowerCase();
        if ((lower.contains("no final") || lower.contains("at the end") || lower.contains("no desfecho")) 
            && (lower.contains("protagonista") || lower.contains("herói") || lower.contains("salva") || lower.contains("surpresa"))) {
            return new SpoilerAnalysisResult(true, 0.88, "high", "Revela desfecho e acontecimentos importantes da história.");
        }

        return new SpoilerAnalysisResult(false, 0.0, "none", "");
    }

    private String elevateLevel(String current, String candidate) {
        if ("high".equalsIgnoreCase(current) || "high".equalsIgnoreCase(candidate)) return "high";
        if ("medium".equalsIgnoreCase(current) || "medium".equalsIgnoreCase(candidate)) return "medium";
        return "low";
    }

    private List<TextSegment> extractSentences(String text) {
        List<TextSegment> list = new ArrayList<>();
        Pattern sentencePattern = Pattern.compile("[^.!?\\n]+[.!?\\n]*");
        Matcher matcher = sentencePattern.matcher(text);

        while (matcher.find()) {
            String sentence = matcher.group();
            if (!sentence.trim().isEmpty()) {
                list.add(new TextSegment(sentence, matcher.start(), matcher.end()));
            }
        }
        return list;
    }

    private record TextSegment(String text, int start, int end) {}
    private record SpoilerPatternRule(Pattern pattern, String level, String reason) {}
    private record SpoilerAnalysisResult(boolean containsSpoiler, double confidence, String level, String reason) {}
}
