# DSCboxd

O **DSCboxd** é um sistema web inspirado na rede social Letterboxd, projetado para o gerenciamento, acompanhamento e avaliação de filmes. O projeto foi desenvolvido como trabalho prático para a disciplina **Desenvolvimento de Sistemas Corporativos (DSC)** do curso de Sistemas de Informação da Universidade Federal da Paraíba (UFPB), Campus IV.

---

## Funcionalidades

O sistema conta com as seguintes funcionalidades reais implementadas:

* **Cadastro e Login de Usuários:** Registro e autenticação de contas através do sistema com diferenciação de permissões baseadas em perfis (`USER` e `ADMIN`).
* **Autenticação JWT:** Acesso aos endpoints protegidos garantido através de JSON Web Tokens (JWT) trafegados no cabeçalho das requisições de forma *stateless*.
* **Catálogo de Filmes:** Exibição completa da lista de filmes disponíveis para todos os usuários cadastrados e visitantes.
* **Busca de Filmes:** Pesquisa dinâmica de títulos de filmes integrada diretamente no catálogo.
* **Avaliação de Filmes:** Usuários autenticados podem avaliar os filmes atribuindo notas de 1 a 5 estrelas.
* **Comentários:** Possibilidade de publicar opiniões sobre os filmes e, para o perfil administrador (`ADMIN`), excluir comentários indesejados.
* **Diário de Filmes Assistidos:** Registro cronológico de filmes assistidos por cada usuário, permitindo marcar a data da visualização e adicionar anotações/observações pessoais sobre a sessão. Também fornece estatísticas individuais dos filmes assistidos.
* **Watchlist (Quero Assistir):** Gerenciamento de uma lista de filmes que o usuário pretende assistir no futuro, permitindo adicionar, remover, consultar e visualizar estatísticas gerais da sua lista.
* **Perfil do Usuário:** Interface no frontend para visualização de estatísticas consolidadas do usuário (total de filmes assistidos, itens salvos na watchlist, etc.).
* **Área Administrativa:** Funcionalidades de gerenciamento exclusivas para usuários com perfil `ADMIN` para cadastrar novos filmes, remover filmes existentes do catálogo e remover comentários inadequados.
* **Upload de Imagens dos Filmes:** Integração para o envio de imagens de capa de filmes no cadastro administrativo, com armazenamento direto em serviço de Object Storage (S3/MinIO).

---

## Tecnologias Utilizadas

O ecossistema do DSCboxd foi desenvolvido utilizando as seguintes tecnologias:

### Backend
* **Linguagem:** Java 21
* **Framework:** Spring Boot 3.4.5 (Spring MVC, Spring Security, Spring Data JPA)
* **Segurança:** Autenticação baseada em JWT (Java JWT - Auth0)
* **Migrations:** Flyway (controle versionado do banco de dados)
* **Ferramenta de Build:** Maven 3.9+
* **Testes:** JUnit 5, Mockito e Testcontainers (banco de testes dinâmico)

### Frontend
* **Tecnologia Principal:** React + Vite (JavaScript)
* **Estilização:** CSS Vanilla com design moderno e responsivo
* **Biblioteca de Ícones:** Lucide React

### Infraestrutura e Serviços
* **Banco de Dados:** PostgreSQL 16
* **Armazenamento de Arquivos (Object Storage):** AWS SDK S3 + MinIO (Local) / AWS S3 (Nuvem)
* **Containerização:** Docker e Docker Compose

---

## Estrutura do Projeto

A organização dos diretórios do projeto segue a arquitetura de divisões de responsabilidade:

```
projeto-eq09/
├── src/main/java/br/ufpb/dsc/mercado/        # Código-fonte do Backend Java
│   ├── config/                               # Classes de configuração (Security, CORS, S3, DataInitializer)
│   │   └── security/                         # Filtro de Autenticação JWT e Provider de Tokens
│   ├── controller/                           # Controllers HTTP / Endpoints REST expostos
│   ├── domain/                               # Entidades JPA que representam as tabelas do banco
│   ├── dto/                                  # Records Java para transferência de dados (Request/Response)
│   ├── exception/                            # Centralização do tratamento de erros e exceções
│   ├── repository/                           # Interfaces Spring Data JPA para comunicação com BD
│   └── service/                              # Camada de lógica de negócio e transações
├── src/main/resources/
│   └── db/migration/                         # Scripts SQL Flyway de versionamento do banco (V1__ a V4__)
├── frontend/                                 # Código-fonte do Frontend React
│   ├── public/                               # Arquivos estáticos
│   └── src/                                  # Componentes, Páginas (Home, Diario, Watchlist, Profile, etc.) e Estilos
├── docker/                                   # Arquivos Dockerfile e docker-compose para ambiente local e produção
├── docs/                                     # Documentações adicionais do projeto
└── cobertura/                                # Relatórios gerados para cobertura de testes
```

---

## Como Executar

Para subir e rodar o projeto localmente com todo o seu ambiente configurado, siga as etapas abaixo:

### Pré-requisitos
* Docker Desktop instalado e em execução.
* JDK 21 instalado localmente.
* Maven 3.9+ (caso queira executar a aplicação backend fora do Docker).
* Node.js v20+ e npm.

### Passo 1: Subir a Infraestrutura (Banco + Storage)
Suba os containers do PostgreSQL, MinIO (S3 local) e Adminer executando na raiz do projeto:
```bash
docker compose -f docker/docker-compose.dev.yml up postgres adminer minio
```

### Passo 2: Inicializar o Backend
Com a infraestrutura ativa, você pode iniciar o backend de duas formas:

**Opção A: Localmente via Maven**
Configure a variável `JAVA_HOME` para o JDK 21 de sua máquina e execute o Maven:
```bash
# Windows (PowerShell)
$env:JAVA_HOME="C:\caminho\para\seu\jdk-21"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
& ".\.maven\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run

# Linux / macOS
export JAVA_HOME="/caminho/para/seu/jdk-21"
export PATH="$JAVA_HOME/bin:$PATH"
mvn spring-boot:run
```

**Opção B: Via Docker Compose (Completo)**
Caso prefira subir tudo em containers, execute apenas:
```bash
docker compose -f docker/docker-compose.dev.yml up
```

### Passo 3: Inicializar o Frontend
Em um novo terminal, entre na pasta do frontend, instale as dependências e execute o servidor de desenvolvimento:
```bash
cd frontend
npm install
npm run dev
```

### Passo 4: Acessar a Aplicação
* **Frontend:** [http://localhost:5173](http://localhost:5173)
* **Backend API:** [http://localhost:8080](http://localhost:8080)
* **Adminer (Visualizar Banco):** [http://localhost:8888](http://localhost:8888) (Servidor: `postgres`, Banco: `mercado_dev`, Usuário: `mercado`, Senha: `mercado123`)
* **MinIO Console (S3 local):** [http://localhost:9001](http://localhost:9001) (Usuário: `eq09`, Senha: `01nXvfGS3LUOABjTxleE0Jy7`)

*Conta inicial para teste (Seed padrão):*
* **Usuário Comum:** `user` / `user123`
* **Administrador:** `admin` / `admin123`

---

## Cobertura de Testes

O projeto conta com uma suíte de testes unitários e de integração abrangente para garantir o correto funcionamento das regras de negócio.

* **Total de Testes Automatizados:** 91 testes executados
* **Cobertura de linhas:** **85,46%** (341 de 399 linhas)
* **Cobertura de branches:** **87,50%** (56 de 64 branches)
* **Resultado de Build:** `BUILD SUCCESS`

> O relatório de cobertura detalhado gerado pelo JaCoCo está disponível no repositório no caminho:
> **[cobertura/jacoco/index.html](file:///c:/Users/Windows%2011/projeto-eq09/cobertura/jacoco/index.html)**

---

## Log de Auditoria

O sistema de auditoria registra e rastreia ações críticas efetuadas na plataforma para fins de monitoramento e segurança.

* **O que é auditado:**
  * Registro de novos usuários (`REGISTRO_USUARIO`)
  * Tentativas de login bem-sucedidas (`LOGIN_SUCESSO`) e falhas de login (`LOGIN_FALHA`)
  * Cadastro de novos filmes (`CADASTRAR_FILME`) e sua exclusão (`DELETAR_FILME`)
  * Criação de novos comentários (`COMENTAR_FILME`) e remoção de comentários por administradores (`DELETAR_COMENTARIO`)
  * Avaliação de filmes (`AVALIAR_FILME`)
  * Marcação (`MARCAR_ASSISTIDO`) e desmarcação (`REMOVER_ASSISTIDO`) de filmes assistidos no diário
  * Adição (`ADICIONAR_WATCHLIST`) e remoção (`REMOVER_WATCHLIST`) de títulos da watchlist
* **Onde os logs são armazenados:** Na tabela `log_auditoria` do banco de dados PostgreSQL. Ela é mapeada através da entidade [LogAuditoria.java](file:///c:/Users/Windows%2011/projeto-eq09/src/main/java/br/ufpb/dsc/mercado/domain/LogAuditoria.java), registrando: identificador único, nome do usuário (se autenticado), a ação executada, detalhes adicionais em texto, IP de origem da requisição e data/hora (`criado_em`).
* **Como foi implementado:** A lógica foi centralizada no serviço [LogAuditoriaService.java](file:///c:/Users/Windows%2011/projeto-eq09/src/main/java/br/ufpb/dsc/mercado/service/LogAuditoriaService.java). Os métodos de persistência do log são configurados com `@Transactional(propagation = Propagation.REQUIRES_NEW)` para garantir que as entradas de auditoria sejam gravadas em uma transação isolada, persistindo mesmo que a transação principal que causou o log sofra rollback. O IP é resolvido a partir dos atributos de requisição via `RequestContextHolder` (suportando cabeçalhos de proxy como `X-Forwarded-For`) e o nome do usuário ativo é obtido via `SecurityContextHolder`.
* **Classes envolvidas:**
  * [LogAuditoria.java](file:///c:/Users/Windows%2011/projeto-eq09/src/main/java/br/ufpb/dsc/mercado/domain/LogAuditoria.java) (Entidade de banco)
  * [LogAuditoriaRepository.java](file:///c:/Users/Windows%2011/projeto-eq09/src/main/java/br/ufpb/dsc/mercado/repository/LogAuditoriaRepository.java) (Interface de acesso a dados)
  * [LogAuditoriaService.java](file:///c:/Users/Windows%2011/projeto-eq09/src/main/java/br/ufpb/dsc/mercado/service/LogAuditoriaService.java) (Serviço agregador de lógica de auditoria)
  * Classes de negócio que invocam o registro:
    * [UsuarioService.java](file:///c:/Users/Windows%2011/projeto-eq09/src/main/java/br/ufpb/dsc/mercado/service/UsuarioService.java)
    * [FilmeService.java](file:///c:/Users/Windows%2011/projeto-eq09/src/main/java/br/ufpb/dsc/mercado/service/FilmeService.java)
    * [ComentarioService.java](file:///c:/Users/Windows%2011/projeto-eq09/src/main/java/br/ufpb/dsc/mercado/service/ComentarioService.java)
    * [AvaliacaoService.java](file:///c:/Users/Windows%2011/projeto-eq09/src/main/java/br/ufpb/dsc/mercado/service/AvaliacaoService.java)
    * [DiarioFilmeService.java](file:///c:/Users/Windows%2011/projeto-eq09/src/main/java/br/ufpb/dsc/mercado/service/DiarioFilmeService.java)
    * [WatchlistService.java](file:///c:/Users/Windows%2011/projeto-eq09/src/main/java/br/ufpb/dsc/mercado/service/WatchlistService.java)

---

## Integração com Serviço Externo

Para o armazenamento persistente de imagens de capas de filmes e mídias do sistema, a aplicação integra-se com serviços de Object Storage compatíveis com S3.

* **Serviço externo:** **MinIO** em ambiente de desenvolvimento local e **AWS S3** em produção.
* **Para que é utilizado:** Upload e hospedagem estática de imagens de capa de filmes enviadas pelo painel administrativo.
* **Como funciona:**
  1. O administrador envia a imagem via formulário HTTP `POST` para o endpoint `/api/upload`.
  2. O [UploadController.java](file:///c:/Users/Windows%2011/projeto-eq09/src/main/java/br/ufpb/dsc/mercado/controller/UploadController.java) intercepta a requisição, valida o arquivo (garantindo que não é vazio e pertence aos tipos MIME permitidos: JPEG, PNG, GIF, WebP).
  3. O arquivo é encaminhado ao [S3StorageService.java](file:///c:/Users/Windows%2011/projeto-eq09/src/main/java/br/ufpb/dsc/mercado/service/S3StorageService.java), que gera uma chave única (UUID) para evitar conflitos, configura a permissão do objeto para leitura pública (`PUBLIC_READ`) e faz o envio à API S3.
  4. O serviço retorna a URL pública gerada para a imagem, que é armazenada na entidade do filme.
* **Classes envolvidas:**
  * [UploadController.java](file:///c:/Users/Windows%2011/projeto-eq09/src/main/java/br/ufpb/dsc/mercado/controller/UploadController.java) (Interceptador REST da requisição)
  * [S3Config.java](file:///c:/Users/Windows%2011/projeto-eq09/src/main/java/br/ufpb/dsc/mercado/config/S3Config.java) (Configurador do `S3Client` da AWS SDK)
  * [S3StorageService.java](file:///c:/Users/Windows%2011/projeto-eq09/src/main/java/br/ufpb/dsc/mercado/service/S3StorageService.java) (Serviço que gerencia o upload do arquivo para o bucket)
* **Variáveis de ambiente de configuração (sem expor credenciais):**
  * `AWS_S3_ENDPOINT`: URL da API do serviço S3 (MinIO local ou AWS endpoint).
  * `AWS_S3_PUBLIC_ENDPOINT`: URL pública para acesso direto de visualização dos arquivos no navegador.
  * `AWS_S3_BUCKET`: Nome do bucket utilizado para armazenar os arquivos.
  * `AWS_S3_ACCESS_KEY`: ID da chave de acesso para o S3.
  * `AWS_S3_SECRET_KEY`: Chave de acesso secreta do S3.
  * `AWS_S3_REGION`: Região geográfica onde o bucket está hospedado.

## Teste de Carga e Performance

Para avaliar o comportamento da aplicação sob estresse, tempo de resposta e capacidade de processamento concorrente, foi realizada uma suíte de testes de carga.

### Ferramenta e Ambiente de Teste
* **Ferramenta:** [k6](https://k6.io)
* **Ambiente:** Aplicação executada localmente via Docker Compose.
* **URL Base de Teste:** `http://localhost:8109`

### Cenários Simulados
Os testes foram estruturados em três fluxos paralelos que simulam o comportamento de uso real na plataforma:

1. **Navegação Pública (Anônima):**
   * `GET /ping` (Saúde da aplicação)
   * `GET /api/filmes` (Visualização do catálogo)
   * `GET /api/filmes?busca=Inception` (Pesquisa de filmes)
   * `GET /api/filmes/{id}` (Visualização de detalhes de filmes)
   * `GET /api/filmes/{id}/comentarios` (Leitura de opiniões de outros usuários)

2. **Fluxo do Usuário Autenticado:**
   * `POST /api/auth/login` (Autenticação do usuário e captura de token JWT)
   * `GET /api/diario/estatisticas` (Acesso às estatísticas do diário de filmes)
   * `GET /api/watchlist` (Listagem dos filmes salvos na watchlist)
   * `GET /api/watchlist/total` (Totalizadores da watchlist)
   * `GET /api/watchlist/filmes/{id}/verificar` (Verificação se um filme específico está na watchlist)
   * `POST /api/watchlist/filmes/{id}` (Adição temporária na watchlist)
   * `DELETE /api/watchlist/filmes/{id}` (Remoção da watchlist para limpeza e conservação de dados)

3. **Fluxo Administrativo:**
   * `POST /api/auth/login` (Autenticação como administrador)
   * `POST /api/filmes` (Cadastro de novo filme no catálogo)
   * `GET /api/filmes/{id}` (Obtenção dos detalhes do filme criado)
   * `DELETE /api/filmes/{id}` (Remoção imediata do filme cadastrado para limpeza de dados)

### Resultados Obtidos
Durante a simulação de carga progressiva, foram medidos os seguintes indicadores de sucesso:
* **Capacidade Máxima:** Sucesso na execução estável com até **74 usuários virtuais (VUs) simultâneos**.
* **Ponto de Saturação:** Saturação apresentada a partir de aproximadamente **75 VUs**, quando o tempo de resposta aumentou drasticamente (cerca de 30 segundos).
* **Tempo de Resposta (p95):** **13,71 ms** (95% das requisições foram atendidas em menos de 13,71 ms antes de atingir a saturação).
* **Taxa de Erro HTTP:** **0%** de requisições falhas.
* **Checks:** **100%** de aprovação nas asserções de validação (incluindo validação de status HTTP, presença do token JWT nas credenciais e integridade estrutural das respostas).
* **Thresholds:** Todas as metas de qualidade configuradas foram plenamente atendidas.

### Análise de Gargalos e Pontos de Melhoria
A partir da saturação identificada sob carga de 75 VUs simultâneos, foram elencadas melhorias arquiteturais para otimizar o desempenho do sistema:
* **Otimização de Consultas SQL:** Refatoração de queries do JPA/Hibernate para evitar problemas clássicos de carregamento excessivo ou consultas redundantes.
* **Criação de Índices:** Mapeamento de índices específicos para as colunas mais utilizadas em cláusulas de filtros de busca no banco PostgreSQL.
* **Ajuste de Connection Pool:** Sintonia fina do pool de conexões (HikariCP) do Spring Boot e configurações do PostgreSQL para suportar maior concorrência.
* **Mecanismos de Cache:** Uso de cache para dados frequentemente acessados (como o catálogo de filmes).
* **Escalabilidade Horizontal:** Distribuição da carga entre múltiplas instâncias da aplicação Spring Boot através de um balanceador de carga.

---

## Equipe

* **Iury Gabriel Andrade da Silva** (@Iurygab14)
* **Luciano Junior** (@lucianovdsjr)
