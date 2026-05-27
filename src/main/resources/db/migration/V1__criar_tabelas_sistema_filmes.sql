-- Migração V1: Criação das tabelas do CineAvalia
-- Tabelas: usuario, filme, avaliacao, comentario

CREATE TABLE usuario (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50) UNIQUE NOT NULL,
    password      VARCHAR(255) NOT NULL,
    role          VARCHAR(20) NOT NULL,
    criado_em     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE filme (
    id            BIGSERIAL PRIMARY KEY,
    titulo        VARCHAR(150) NOT NULL,
    diretor       VARCHAR(100),
    ano           INTEGER NOT NULL,
    sinopse       TEXT,
    genero        VARCHAR(50),
    imagem_url    VARCHAR(500),
    criado_em     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE avaliacao (
    id            BIGSERIAL PRIMARY KEY,
    usuario_id    BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    filme_id      BIGINT NOT NULL REFERENCES filme(id) ON DELETE CASCADE,
    nota          INTEGER NOT NULL CHECK (nota >= 1 AND nota <= 5),
    criado_em     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_usuario_filme UNIQUE (usuario_id, filme_id)
);

CREATE TABLE comentario (
    id            BIGSERIAL PRIMARY KEY,
    usuario_id    BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    filme_id      BIGINT NOT NULL REFERENCES filme(id) ON DELETE CASCADE,
    texto         TEXT NOT NULL,
    criado_em     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Índices para otimização de consultas
CREATE INDEX idx_usuario_username ON usuario(username);
CREATE INDEX idx_filme_titulo ON filme(titulo);
CREATE INDEX idx_avaliacao_filme ON avaliacao(filme_id);
CREATE INDEX idx_comentario_filme ON comentario(filme_id);

-- Comentários descritivos
COMMENT ON TABLE usuario IS 'Tabela de usuários do sistema CineAvalia';
COMMENT ON TABLE filme IS 'Tabela de filmes cadastrados no CineAvalia';
COMMENT ON TABLE avaliacao IS 'Tabela de avaliações de filmes feitas pelos usuários (nota 1 a 5)';
COMMENT ON TABLE comentario IS 'Tabela de comentários feitos pelos usuários nos filmes';
