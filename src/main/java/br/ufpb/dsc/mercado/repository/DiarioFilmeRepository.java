package br.ufpb.dsc.mercado.repository;

import br.ufpb.dsc.mercado.domain.DiarioFilme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiarioFilmeRepository extends JpaRepository<DiarioFilme, Long> {
    Optional<DiarioFilme> findByUsuarioIdAndFilmeId(Long usuarioId, Long filmeId);
    List<DiarioFilme> findByUsuarioIdOrderByDataAssistidoDesc(Long usuarioId);
    boolean existsByUsuarioIdAndFilmeId(Long usuarioId, Long filmeId);
    long countByUsuarioId(Long usuarioId);
    void deleteByUsuarioIdAndFilmeId(Long usuarioId, Long filmeId);
}
