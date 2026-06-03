# =============================================================================
# Dockerfile de PRODUÇÃO — Multi-stage build (Frontend + Backend)
# =============================================================================
# Estágio 1 (frontend-builder): constrói a aplicação React com Vite.
# Estágio 2 (backend-builder): copia os assets compilados do React para a pasta
#                              static do Spring, compila o projeto com Maven e gera o JAR.
# Estágio 3 (runtime): imagem mínima com apenas o JRE, rodando a aplicação.
# =============================================================================

# ---- Estágio 1: Frontend Build ----------------------------------------------
FROM node:20-alpine AS frontend-builder

WORKDIR /frontend

# Copiar arquivos de dependências
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --silent

# Copiar código-fonte e compilar
COPY frontend/ ./
RUN npm run build

# ---- Estágio 2: Backend Build -----------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS backend-builder

WORKDIR /build

# Copiar pom.xml primeiro para cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

# Copiar código-fonte
COPY src ./src

# Copiar os arquivos estáticos do React compilados para a pasta static do Spring Boot
# Desta forma, o Spring Boot servirá o frontend na rota raiz '/'
COPY --from=frontend-builder /frontend/dist ./src/main/resources/static

# Compilar o backend Java gerando o arquivo JAR
RUN mvn clean package -DskipTests -B -q

# ---- Estágio 3: Runtime -----------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime

# Criar usuário não-root por segurança
RUN groupadd --gid 1001 mercado && \
    useradd --uid 1001 --gid mercado --shell /bin/false mercado

WORKDIR /app

# Copiar apenas o JAR do estágio anterior
COPY --from=backend-builder /build/target/*.jar app.jar

# Definir owner do arquivo
RUN chown mercado:mercado app.jar

# Trocar para usuário não-root
USER mercado

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Dspring.profiles.active=prod", \
    "-jar", "app.jar"]
