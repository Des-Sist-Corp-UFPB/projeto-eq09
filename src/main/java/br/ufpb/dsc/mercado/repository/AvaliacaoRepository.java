package br.ufpb.dsc.mercado.repository;

import br.ufpb.dsc.mercado.domain.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    Optional<Avaliacao> findByUsuarioIdAndFilmeId(Long usuarioId, Long filmeId);
    List<Avaliacao> findByFilmeId(Long filmeId);

    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.filme.id = :filmeId")
    Double getAverageNotaByFilmeId(@Param("filmeId") Long filmeId);

    long countByFilmeId(Long filmeId);

    long countByUsuarioId(Long usuarioId);

    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.usuario.id = :usuarioId")
    Double getAverageNotaByUsuarioId(@Param("usuarioId") Long usuarioId);
}

