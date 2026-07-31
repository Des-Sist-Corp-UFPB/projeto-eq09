package br.ufpb.dsc.mercado.service;

import br.ufpb.dsc.mercado.domain.DiarioFilme;
import br.ufpb.dsc.mercado.domain.Filme;
import br.ufpb.dsc.mercado.domain.Usuario;
import br.ufpb.dsc.mercado.dto.ChatResponse;
import br.ufpb.dsc.mercado.dto.FilmeRecomendacaoDTO;
import br.ufpb.dsc.mercado.repository.DiarioFilmeRepository;
import br.ufpb.dsc.mercado.repository.FilmeRepository;
import br.ufpb.dsc.mercado.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private final UsuarioRepository usuarioRepository;
    private final FilmeRepository filmeRepository;
    private final DiarioFilmeRepository diarioFilmeRepository;
    private final LlmService llmService;

    public ChatbotService(UsuarioRepository usuarioRepository,
                          FilmeRepository filmeRepository,
                          DiarioFilmeRepository diarioFilmeRepository,
                          LlmService llmService) {
        this.usuarioRepository = usuarioRepository;
        this.filmeRepository = filmeRepository;
        this.diarioFilmeRepository = diarioFilmeRepository;
        this.llmService = llmService;
    }

    @Transactional(readOnly = true)
    public ChatResponse conversar(String username, String mensagem) {
        if (mensagem == null || mensagem.isBlank()) {
            return new ChatResponse("Olá! Como posso ajudar você a escolher seu próximo filme hoje?", Collections.emptyList());
        }

        // Obtém o usuário e histórico de assistidos
        Optional<Usuario> usuarioOpt = (username != null && !username.isBlank()) 
                ? usuarioRepository.findByUsername(username) 
                : Optional.empty();

        List<DiarioFilme> assistidos = usuarioOpt.isPresent()
                ? diarioFilmeRepository.findByUsuarioIdOrderByDataAssistidoDesc(usuarioOpt.get().getId())
                : Collections.emptyList();

        List<Filme> todosFilmes = filmeRepository.findAll();

        Set<Long> idsAssistidos = assistidos.stream()
                .map(d -> d.getFilme().getId())
                .collect(Collectors.toSet());

        // Filmes não assistidos para recomendar
        List<Filme> candidatos = todosFilmes.stream()
                .filter(f -> !idsAssistidos.contains(f.getId()))
                .collect(Collectors.toList());

        if (candidatos.isEmpty()) {
            candidatos = new ArrayList<>(todosFilmes);
        }

        if (todosFilmes.isEmpty()) {
            return new ChatResponse(
                "No momento não temos filmes cadastrados no catálogo para sugerir. Que tal adicionar alguns filmes no sistema?",
                Collections.emptyList()
            );
        }

        // Tenta gerar resposta através da LLM (LiteLLM / OpenAI API)
        if (llmService != null) {
            String promptSistema = "Você é o assistente virtual DSCbot do sistema de recomendação de filmes DSCboxd. " +
                    "Seu objetivo é ajudar usuários recomendando filmes do nosso catálogo com base nas suas preferências e histórico do diário. " +
                    "Responda sempre em português de forma simpática, clara e entusiasmada.";

            StringBuilder promptUsuarioBuilder = new StringBuilder();
            promptUsuarioBuilder.append("Histórico de filmes assistidos pelo usuário no diário:\n");
            if (assistidos.isEmpty()) {
                promptUsuarioBuilder.append("- Nenhum filme registrado no diário ainda.\n");
            } else {
                for (DiarioFilme df : assistidos) {
                    Filme f = df.getFilme();
                    promptUsuarioBuilder.append(String.format("- %s (%d) - Gênero: %s, Diretor: %s\n",
                            f.getTitulo(), f.getAno(), f.getGenero(), f.getDiretor()));
                }
            }

            promptUsuarioBuilder.append("\nCatálogo de filmes disponíveis para sugestão:\n");
            for (Filme f : candidatos) {
                promptUsuarioBuilder.append(String.format("- [ID: %d] %s (%d) - Gênero: %s, Diretor: %s\n",
                        f.getId(), f.getTitulo(), f.getAno(), f.getGenero(), f.getDiretor()));
            }

            promptUsuarioBuilder.append("\nMensagem do usuário: ").append(mensagem);

            String respostaLlm = llmService.gerarResposta(promptSistema, promptUsuarioBuilder.toString());
            if (respostaLlm != null && !respostaLlm.isBlank()) {
                List<FilmeRecomendacaoDTO> recomendacoes = selecionarRecomendacoesParaLlm(candidatos, respostaLlm, mensagem);
                return new ChatResponse(respostaLlm, recomendacoes);
            }
        }

        // Fallback gracioso para algoritmo baseado em regras se a LLM falhar ou não estiver disponível
        return conversarFallbackRegras(mensagem, assistidos, candidatos);
    }

    private List<FilmeRecomendacaoDTO> selecionarRecomendacoesParaLlm(List<Filme> candidatos, String respostaLlm, String mensagem) {
        List<FilmeRecomendacaoDTO> recomendacoes = new ArrayList<>();
        String respostaLower = respostaLlm.toLowerCase();
        String msgLower = mensagem.toLowerCase();
        String generoProcurado = identificarGeneroNaMensagem(msgLower);

        // 1. Filmes explicitamente citados no texto da LLM
        for (Filme f : candidatos) {
            if (f.getTitulo() != null && respostaLower.contains(f.getTitulo().toLowerCase())) {
                recomendacoes.add(new FilmeRecomendacaoDTO(
                        f.getId(), f.getTitulo(), f.getGenero(), f.getDiretor(), f.getAno(), f.getImagemUrl(),
                        "Recomendado pela IA DSCbot."
                ));
                if (recomendacoes.size() >= 3) break;
            }
        }

        // 2. Se a IA não citou nomes exatos de filmes, busca por gênero solicitado
        if (recomendacoes.size() < 3 && generoProcurado != null) {
            for (Filme f : candidatos) {
                if (recomendacoes.stream().noneMatch(r -> r.id().equals(f.getId()))) {
                    if (f.getGenero() != null && f.getGenero().toLowerCase().contains(generoProcurado.toLowerCase())) {
                        recomendacoes.add(new FilmeRecomendacaoDTO(
                                f.getId(), f.getTitulo(), f.getGenero(), f.getDiretor(), f.getAno(), f.getImagemUrl(),
                                "Sugerido com base no gênero " + f.getGenero()
                        ));
                        if (recomendacoes.size() >= 3) break;
                    }
                }
            }
        }

        // 3. Completa com candidatos disponíveis
        if (recomendacoes.size() < 3) {
            for (Filme f : candidatos) {
                if (recomendacoes.stream().noneMatch(r -> r.id().equals(f.getId()))) {
                    recomendacoes.add(new FilmeRecomendacaoDTO(
                            f.getId(), f.getTitulo(), f.getGenero(), f.getDiretor(), f.getAno(), f.getImagemUrl(),
                            "Destaque do catálogo."
                    ));
                    if (recomendacoes.size() >= 3) break;
                }
            }
        }

        return recomendacoes;
    }

    private ChatResponse conversarFallbackRegras(String mensagem, List<DiarioFilme> assistidos, List<Filme> candidatos) {
        String msgLower = mensagem.trim().toLowerCase();
        Set<String> generosAssistidos = assistidos.stream()
                .map(d -> d.getFilme().getGenero())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> diretoresAssistidos = assistidos.stream()
                .map(d -> d.getFilme().getDiretor())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        String generoProcurado = identificarGeneroNaMensagem(msgLower);
        List<FilmeRecomendacaoDTO> recomendacoes = new ArrayList<>();
        StringBuilder respostaBuilder = new StringBuilder();

        if (generoProcurado != null) {
            List<Filme> porGenero = candidatos.stream()
                    .filter(f -> f.getGenero() != null && f.getGenero().toLowerCase().contains(generoProcurado.toLowerCase()))
                    .limit(3)
                    .collect(Collectors.toList());

            if (!porGenero.isEmpty()) {
                respostaBuilder.append("Com base no seu pedido de filmes de **").append(generoProcurado).append("**, aqui estão as melhores opções para você:\n");
                for (Filme f : porGenero) {
                    recomendacoes.add(new FilmeRecomendacaoDTO(
                        f.getId(), f.getTitulo(), f.getGenero(), f.getDiretor(), f.getAno(), f.getImagemUrl(),
                        "Filme do gênero " + f.getGenero() + " disponível no catálogo."
                    ));
                }
            } else {
                respostaBuilder.append("Não encontrei lançamentos específicos de ").append(generoProcurado).append(" não assistidos no catálogo, mas aqui estão ótimas sugestões de outros gêneros:\n");
                adicionarSugestoesPadrao(candidatos, recomendacoes);
            }
        } else if (!assistidos.isEmpty()) {
            String titulosAssistidos = assistidos.stream()
                    .limit(3)
                    .map(d -> "\"" + d.getFilme().getTitulo() + "\"")
                    .collect(Collectors.joining(", "));

            respostaBuilder.append("Analisando o seu histórico de diário, vi que você assistiu recentemente a: ")
                    .append(titulosAssistidos).append(".\n\n");

            List<Filme> semelhantes = candidatos.stream()
                    .filter(f -> (f.getGenero() != null && generosAssistidos.contains(f.getGenero())) ||
                                 (f.getDiretor() != null && diretoresAssistidos.contains(f.getDiretor())))
                    .limit(3)
                    .collect(Collectors.toList());

            if (!semelhantes.isEmpty()) {
                respostaBuilder.append("Com base nos seus gêneros e diretores favoritos, recomendo fortissimamente:");
                for (Filme f : semelhantes) {
                    recomendacoes.add(new FilmeRecomendacaoDTO(
                        f.getId(), f.getTitulo(), f.getGenero(), f.getDiretor(), f.getAno(), f.getImagemUrl(),
                        "Recomendado por combinar com os filmes do gênero " + f.getGenero() + " que você já assistiu."
                    ));
                }
            } else {
                respostaBuilder.append("Aqui estão as principais dicas de filmes do catálogo para expandir seu repertório:");
                adicionarSugestoesPadrao(candidatos, recomendacoes);
            }
        } else {
            respostaBuilder.append("Olá! Vi que você ainda não marcou filmes no seu diário de assistidos. ")
                    .append("Para começar a construir seu histórico, aqui estão excelentes sugestões do nosso catálogo:");

            adicionarSugestoesPadrao(candidatos, recomendacoes);
        }

        return new ChatResponse(respostaBuilder.toString(), recomendacoes);
    }

    private void adicionarSugestoesPadrao(List<Filme> candidatos, List<FilmeRecomendacaoDTO> recomendacoes) {
        List<Filme> selecionados = candidatos.stream().limit(3).collect(Collectors.toList());
        for (Filme f : selecionados) {
            recomendacoes.add(new FilmeRecomendacaoDTO(
                f.getId(), f.getTitulo(), f.getGenero(), f.getDiretor(), f.getAno(), f.getImagemUrl(),
                "Popular no catálogo."
            ));
        }
    }

    private String identificarGeneroNaMensagem(String msg) {
        if (msg.contains("ação") || msg.contains("acao")) return "Ação";
        if (msg.contains("ficção") || msg.contains("ficcao") || msg.contains("sci-fi") || msg.contains("scifi")) return "Ficção Científica";
        if (msg.contains("drama")) return "Drama";
        if (msg.contains("comédia") || msg.contains("comedia")) return "Comédia";
        if (msg.contains("terror") || msg.contains("horror")) return "Terror";
        if (msg.contains("suspense") || msg.contains("thriller")) return "Suspense";
        if (msg.contains("romance")) return "Romance";
        if (msg.contains("animação") || msg.contains("animacao")) return "Animação";
        if (msg.contains("aventura")) return "Aventura";
        return null;
    }
}
