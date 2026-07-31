-- Migração V4: Criação da tabela watchlist para salvar filmes que os usuários querem assistir futuramente
CREATE TABLE watchlist (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    filme_id    BIGINT NOT NULL REFERENCES filme(id) ON DELETE CASCADE,
    data_adicao TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_usuario_filme_watchlist UNIQUE (usuario_id, filme_id)
);

-- Índices para otimização de consultas
CREATE INDEX idx_watchlist_usuario ON watchlist(usuario_id);
CREATE INDEX idx_watchlist_filme ON watchlist(filme_id);

-- Comentários descritivos
COMMENT ON TABLE watchlist IS 'Tabela que armazena os filmes que os usuários pretendem assistir futuramente';
COMMENT ON COLUMN watchlist.usuario_id IS 'ID do usuário associado';
COMMENT ON COLUMN watchlist.filme_id IS 'ID do filme associado';
COMMENT ON COLUMN watchlist.data_adicao IS 'Data e hora em que o filme foi adicionado à watchlist';
