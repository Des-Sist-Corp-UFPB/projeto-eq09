package br.ufpb.dsc.mercado.repository;

import br.ufpb.dsc.mercado.domain.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByFilmeIdOrderByCriadoEmDesc(Long filmeId);
}
