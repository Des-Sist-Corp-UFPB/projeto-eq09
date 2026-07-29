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

    public ChatbotService(UsuarioRepository usuarioRepository,
                          FilmeRepository filmeRepository,
                          DiarioFilmeRepository diarioFilmeRepository) {
        this.usuarioRepository = usuarioRepository;
        this.filmeRepository = filmeRepository;
        this.diarioFilmeRepository = diarioFilmeRepository;
    }

    @Transactional(readOnly = true)
    public ChatResponse conversar(String username, String mensagem) {
        if (mensagem == null || mensagem.isBlank()) {
            return new ChatResponse("Olá! Como posso ajudar você a escolher seu próximo filme hoje?", Collections.emptyList());
        }

        String msgLower = mensagem.trim().toLowerCase();

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

        Set<String> generosAssistidos = assistidos.stream()
                .map(d -> d.getFilme().getGenero())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> diretoresAssistidos = assistidos.stream()
                .map(d -> d.getFilme().getDiretor())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Identifica solicitações de gêneros específicos na mensagem do usuário
        String generoProcurado = identificarGeneroNaMensagem(msgLower);

        // Filmes não assistidos para recomendar
        List<Filme> candidatos = todosFilmes.stream()
                .filter(f -> !idsAssistidos.contains(f.getId()))
                .collect(Collectors.toList());

        // Se o catálogo não assistido estiver vazio, considera todo o catálogo
        if (candidatos.isEmpty()) {
            candidatos = new ArrayList<>(todosFilmes);
        }

        List<FilmeRecomendacaoDTO> recomendacoes = new ArrayList<>();
        StringBuilder respostaBuilder = new StringBuilder();

        if (candidatos.isEmpty()) {
            return new ChatResponse(
                "No momento não temos filmes cadastrados no catálogo para sugerir. Que tal adicionar alguns filmes no sistema?",
                Collections.emptyList()
            );
        }

        // Se o usuário pediu um gênero específico (ex: "sugira filmes de ação", "quero um suspense")
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
        } 
        // Se o usuário já assistiu a filmes e pede recomendação geral ou conversa
        else if (!assistidos.isEmpty()) {
            String titulosAssistidos = assistidos.stream()
                    .limit(3)
                    .map(d -> "\"" + d.getFilme().getTitulo() + "\"")
                    .collect(Collectors.joining(", "));

            respostaBuilder.append("Analisando o seu histórico de diário, vi que você assistiu recentemente a: ")
                    .append(titulosAssistidos).append(".\n\n");

            // Prioriza filmes com gêneros ou diretores semelhantes aos assistidos
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
        } 
        // Se é um usuário novo ou sem filmes marcados no diário
        else {
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
