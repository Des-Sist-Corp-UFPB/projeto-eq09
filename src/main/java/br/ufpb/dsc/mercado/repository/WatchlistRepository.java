package br.ufpb.dsc.mercado.repository;

import br.ufpb.dsc.mercado.domain.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    Optional<Watchlist> findByUsuarioIdAndFilmeId(Long usuarioId, Long filmeId);
    List<Watchlist> findByUsuarioIdOrderByDataAdicaoDesc(Long usuarioId);
    boolean existsByUsuarioIdAndFilmeId(Long usuarioId, Long filmeId);
    long countByUsuarioId(Long usuarioId);
    Optional<Watchlist> findFirstByUsuarioIdOrderByDataAdicaoDesc(Long usuarioId);
    void deleteByUsuarioIdAndFilmeId(Long usuarioId, Long filmeId);
}
