package br.ufpb.dsc.mercado.repository;

import br.ufpb.dsc.mercado.domain.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
}
