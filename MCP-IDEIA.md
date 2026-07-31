# Ideia de Servidor MCP — EQ09

**Domínio:** Rede social de filmes (estilo Letterboxd)  
**Data:** 2026-07-01

## O que é

Um **servidor MCP (Model Context Protocol)** expõe as operações do seu sistema como *tools* e *resources* que qualquer assistente de IA (Claude Desktop, Cursor, etc.) pode chamar com segurança. Na prática, é uma camada fina sobre a **API que vocês já têm** — cada tool chama um endpoint/service existente. Assim o projeto deixa de ser só uma tela e passa a ser operável por um agente de IA.

## Servidor proposto: `dscboxd-mcp`

### Tools sugeridas

- `buscar_filme(titulo)` — busca filme
- `avaliar_filme(userId, filmeId, nota)` — registra avaliação
- `recomendar(userId)` — recomendações
- `feed_amigos(userId)` — atividade recente

### Resources (somente leitura)

- catálogo/feed como resource

### Exemplos de uso com um LLM

- "Recomende filmes parecidos com os que dei 5 estrelas."

## Esqueleto para começar (Java / Spring AI)

```java
// pom.xml: org.springframework.ai:spring-ai-starter-mcp-server-webmvc
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class DscboxdTools {

    private final SeuService seuService;   // injete seus services/repositories

    public DscboxdTools(SeuService seuService) { this.seuService = seuService; }

    @Tool(description = "busca filme")
    public Object buscar_filme(/* params */) {
        return seuService.suaOperacaoExistente();   // reaproveite sua lógica
    }
}
```
> Registre as tools com um `MethodToolCallbackProvider` (bean) apontando para esta classe.

## Boas práticas

- **Segurança:** cada tool que altera dados deve exigir autenticação e registrar no **log de auditoria** (o mesmo do requisito da disciplina).
- **Escopo mínimo:** exponha só o necessário; separe tools de leitura das de escrita.
- **Reaproveite:** as tools devem chamar seus *services*/*controllers* existentes, não reimplementar regra de negócio.

## Referências
- Documentação MCP: https://modelcontextprotocol.io
- SDKs: Python (`mcp`), TypeScript (`@modelcontextprotocol/sdk`), Java (Spring AI MCP Server).

*Sugestão gerada em 2026-07-01 para orientar a integração de LLMs ao projeto.*