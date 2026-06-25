# Relatório de Avaliação — EQ09 (DSC)

| | |
|---|---|
| **Data** | 2026-06-25 |
| **Repositório** | https://github.com/des-sist-corp-ufpb/projeto-eq09 |
| **Aplicação** | https://eq09.dsc.rodrigor.com |
| **Período de atividade** | 2026-06-24 → 2026-06-24 |
| **Total de commits** (sem merges) | 1 |
| **Integrantes** | Iury Gabriel Andrade Da Silva (@Iurygab14) |

---

## 1. Tecnologias

- Spring Boot 3.4.5
- Flyway (4 migrations)
- Spring Security
- JWT
- MinIO/S3

---

## 2. Análise Funcional

### Endpoints REST (22 mapeados)

| Método | Path | Arquivo |
|--------|------|---------|
| `POST` | `/api/auth/login` | `AuthController.java` |
| `POST` | `/api/auth/register` | `AuthController.java` |
| `POST` | `/api/filmes/{filmeId}/avaliar` | `AvaliacaoController.java` |
| `DELETE` | `/api/filmes/{filmeId}/comentarios/{comentarioId}` | `ComentarioController.java` |
| `GET` | `/api/filmes/{filmeId}/comentarios` | `ComentarioController.java` |
| `POST` | `/api/filmes/{filmeId}/comentarios` | `ComentarioController.java` |
| `DELETE` | `/api/diario/filmes/{filmeId}` | `DiarioController.java` |
| `GET` | `/api/diario` | `DiarioController.java` |
| `GET` | `/api/diario/estatisticas` | `DiarioController.java` |
| `GET` | `/api/diario/filmes/{filmeId}/verificar` | `DiarioController.java` |
| `POST` | `/api/diario/filmes/{filmeId}` | `DiarioController.java` |
| `DELETE` | `/api/filmes/{id}` | `FilmeController.java` |
| `GET` | `/api/filmes` | `FilmeController.java` |
| `GET` | `/api/filmes/{id}` | `FilmeController.java` |
| `POST` | `/api/filmes` | `FilmeController.java` |
| `GET` | `/ping` | `PingController.java` |
| `POST` | `/api/upload` | `UploadController.java` |
| `DELETE` | `/api/watchlist/filmes/{filmeId}` | `WatchlistController.java` |
| `GET` | `/api/watchlist` | `WatchlistController.java` |
| `GET` | `/api/watchlist/filmes/{filmeId}/verificar` | `WatchlistController.java` |
| `GET` | `/api/watchlist/total` | `WatchlistController.java` |
| `POST` | `/api/watchlist/filmes/{filmeId}` | `WatchlistController.java` |

### Entidades / Tabelas (14 encontradas)

- `watchlist`
- `diario_filme`
- `avaliacao`
- `filme`
- `log_auditoria`
- `usuario`
- `comentario`
- `usuario (via V1__criar_tabelas_sistema_filmes.sql)`
- `filme (via V1__criar_tabelas_sistema_filmes.sql)`
- `avaliacao (via V1__criar_tabelas_sistema_filmes.sql)`
- `comentario (via V1__criar_tabelas_sistema_filmes.sql)`
- `log_auditoria (via V2__criar_tabela_log_auditoria.sql)`
- `diario_filme (via V3__criar_tabela_diario_filmes.sql)`
- `watchlist (via V4__criar_tabela_watchlist.sql)`

### Migrations (4 arquivos)

- `V1__criar_tabelas_sistema_filmes.sql`
- `V2__criar_tabela_log_auditoria.sql`
- `V3__criar_tabela_diario_filmes.sql`
- `V4__criar_tabela_watchlist.sql`

---

## 3. Análise Arquitetural

| Aspecto | Status | Observação |
|---------|--------|-----------|
| Arquitetura em camadas | ✅ | controller=✅  service=✅  repository=✅ |
| Testes automatizados | ✅ | 12 arquivo(s) de teste |
| Migrations versionadas | ✅ | 4 migration(s) |
| Logging | ❌ | não detectado |
| Autenticação / Segurança | ✅ | Spring Security / JWT / decorator detectado |
| DTOs / Separação de dados | ✅ | classes *DTO / *Request / *Response detectadas |
| Tratamento global de exceções | ✅ | @ControllerAdvice / @ExceptionHandler detectado |
| Documentação de API (OpenAPI) | ❌ | não detectado |
| Variáveis de ambiente | ✅ | .env / @Value / os.environ detectado |
| Dockerfile / docker-compose | ✅ | presente |

---

## 4. Contribuição por Usuário

### Resumo

| Usuário | Commits | % commits | Linhas adicionadas | Linhas no código atual | % código atual |
|---------|---------|-----------|-------------------|----------------------|----------------|
| Iury Gabriel Andrade Da Silva (@Iurygab14) | 1 | 100% | 19.353 | 4.727 | 100% |

### Contribuição por Camada

| Camada | Total linhas | Iury Gabriel Andrade Da Silva (@Iurygab14) |
|--------|-------------|---------|
| Controller | 757 | 100% |
| Frontend | 280 | 100% |
| Repository | 104 | 100% |
| Service | 2.210 | 100% |

---

## 5. Contribuição por Funcionalidade

Baseado em `git blame` nos arquivos de controller e service.

| Arquivo | Total linhas | Iury Gabriel Andrade Da Silva (@Iurygab14) |
|---------|-------------|---------|
| `DiarioFilmeServiceTest.java` | 288 | 100% |
| `WatchlistServiceTest.java` | 286 | 100% |
| `ComentarioServiceTest.java` | 167 | 100% |
| `LogAuditoriaServiceTest.java` | 167 | 100% |
| `FilmeServiceTest.java` | 157 | 100% |
| `DiarioControllerTest.java` | 147 | 100% |
| `DiarioFilmeService.java` | 139 | 100% |
| `UsuarioServiceTest.java` | 139 | 100% |
| `WatchlistService.java` | 128 | 100% |
| `AvaliacaoServiceTest.java` | 111 | 100% |
| `FilmeService.java` | 97 | 100% |
| `S3StorageServiceTest.java` | 95 | 100% |
| `UsuarioService.java` | 93 | 100% |
| `ComentarioService.java` | 79 | 100% |
| `LogAuditoriaService.java` | 78 | 100% |
| `WatchlistController.java` | 78 | 100% |
| `UploadControllerTest.java` | 77 | 100% |
| `DiarioController.java` | 76 | 100% |
| `S3StorageService.java` | 57 | 100% |
| `FilmeController.java` | 54 | 100% |
| `ComentarioController.java` | 52 | 100% |
| `V1__criar_tabelas_sistema_filmes.sql` | 51 | 100% |
| `AvaliacaoService.java` | 46 | 100% |
| `UploadController.java` | 44 | 100% |
| `MercadoApplicationTests.java` | 43 | 100% |
| `AuthController.java` | 41 | 100% |
| `MercadoApplication.java` | 40 | 100% |
| `AvaliacaoController.java` | 32 | 100% |
| `PingControllerTest.java` | 30 | 100% |
| `PingController.java` | 21 | 100% |
| `V3__criar_tabela_diario_filmes.sql` | 20 | 100% |
| `V4__criar_tabela_watchlist.sql` | 18 | 100% |
| `V2__criar_tabela_log_auditoria.sql` | 16 | 100% |

---

*Relatório gerado automaticamente em 2026-06-25.*
*Os dados de contribuição são baseados em `git log --numstat` (linhas adicionadas) e `git blame` (linhas no código atual), excluindo commits de merge.*