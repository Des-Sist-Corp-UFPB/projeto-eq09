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

- ✅ **Object Storage (S3/MinIO)** — declarado no README e comprovado no código (36 ocorrência(s)).
  - Evidência: `docker-compose.yml:56:      # Configurações do Object Storage S3 / MinIO`

---

## 3. Cobertura de Testes (≥ 85%)

✅ **Atendido** — linhas 85.5% (instruções 86.9% · ramos 87.5%) [JaCoCo] (relatório em `cobertura/`, 61 arquivo(s)).

> Critério: **cobertura de linhas** ≥ 85% (conforme a orientação). As demais métricas (instruções/ramos) são informativas.

> Observação: a cobertura é lida do relatório commitado pela equipe; não é recalculada nesta avaliação.

---

*Avaliação gerada automaticamente em 2026-07-01. Consulte `ORIENTACOES-AVALIACAO-2026-06-29.md` para os critérios.*