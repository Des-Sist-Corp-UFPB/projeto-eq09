# Avaliação — EQ09 (DSC)

**Data:** 2026-07-01  
**Avaliador:** Prof. Rodrigo  
**Método:** verificação automática cruzando o que o `README.md` declara com evidências no código-fonte (leitura de `origin/main`).

> Esta é uma avaliação automática preliminar. O que não estiver documentado no README e commitado no repositório é considerado não atendido.

---

## 1. Log de Auditoria

✅ **Atendido** — documentado no README e com 81 evidência(s) no código.

---

## 2. Integração com Serviço Externo

- ✅ **AWS S3** — declarado no README e comprovado no código (15 ocorrência(s)).
  - Evidência: `src/main/java/br/ufpb/dsc/mercado/config/S3Config.java:9:import software.amazon.awssdk.services.s3.S3Client;`
- ✅ **MinIO** — declarado no README e comprovado no código (18 ocorrência(s)).
  - Evidência: `docker-compose.yml:56:      # Configurações do Object Storage S3 / MinIO`

---

## 3. Cobertura de Testes (≥ 85%)

✅ **Atendido** — 86% (JaCoCo) jacoco (relatório em `cobertura/`, 61 arquivo(s)).

> Observação: a cobertura é lida do relatório commitado pela equipe; não é recalculada nesta avaliação.

---

*Avaliação gerada automaticamente em 2026-07-01. Consulte `ORIENTACOES-AVALIACAO-2026-06-29.md` para os critérios.*