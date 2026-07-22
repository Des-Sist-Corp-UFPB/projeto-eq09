-- Migração V3: Criação da tabela diario_filme para registrar filmes assistidos
CREATE TABLE diario_filme (
    id             BIGSERIAL PRIMARY KEY,
    usuario_id     BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    filme_id       BIGINT NOT NULL REFERENCES filme(id) ON DELETE CASCADE,
    data_assistido TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    observacao     TEXT,
    CONSTRAINT uk_usuario_filme_diario UNIQUE (usuario_id, filme_id)
);

-- Índices para otimização de consultas
CREATE INDEX idx_diario_usuario ON diario_filme(usuario_id);
CREATE INDEX idx_diario_filme ON diario_filme(filme_id);

-- Comentários descritivos
COMMENT ON TABLE diario_filme IS 'Tabela que armazena o diário de filmes assistidos pelos usuários';
COMMENT ON COLUMN diario_filme.usuario_id IS 'ID do usuário que assistiu o filme';
COMMENT ON COLUMN diario_filme.filme_id IS 'ID do filme que foi assistido';
COMMENT ON COLUMN diario_filme.data_assistido IS 'Data e hora em que o filme foi marcado como assistido';
COMMENT ON COLUMN diario_filme.observacao IS 'Observação ou comentário pessoal opcional sobre o filme assistido';
