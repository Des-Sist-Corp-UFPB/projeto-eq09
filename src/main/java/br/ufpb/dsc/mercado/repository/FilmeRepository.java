package br.ufpb.dsc.mercado.repository;

import br.ufpb.dsc.mercado.domain.Filme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FilmeRepository extends JpaRepository<Filme, Long> {
    List<Filme> findByTituloContainingIgnoreCaseOrderByTituloAsc(String query);
    List<Filme> findAllByOrderByTituloAsc();
    boolean existsByTitulo(String titulo);
}
