-- Migração V2: Criação da tabela log_auditoria
CREATE TABLE log_auditoria (
    id            BIGSERIAL PRIMARY KEY,
    usuario       VARCHAR(100),
    acao          VARCHAR(50) NOT NULL,
    detalhes      TEXT,
    ip            VARCHAR(45),
    criado_em     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Índices para otimização de consultas
CREATE INDEX idx_log_auditoria_usuario ON log_auditoria(usuario);
CREATE INDEX idx_log_auditoria_acao ON log_auditoria(acao);
CREATE INDEX idx_log_auditoria_criado_em ON log_auditoria(criado_em);

COMMENT ON TABLE log_auditoria IS 'Tabela que armazena os logs de auditoria das ações dos usuários';
