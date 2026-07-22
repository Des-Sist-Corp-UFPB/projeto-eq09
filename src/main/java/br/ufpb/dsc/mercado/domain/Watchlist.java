package br.ufpb.dsc.mercado.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "watchlist", uniqueConstraints = {
    @UniqueConstraint(name = "uk_usuario_filme_watchlist", columnNames = {"usuario_id", "filme_id"})
})
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filme_id", nullable = false)
    private Filme filme;

    @Column(name = "data_adicao", nullable = false)
    private Instant dataAdicao;

    @PrePersist
    protected void prePersist() {
        if (this.dataAdicao == null) {
            this.dataAdicao = Instant.now();
        }
    }

    public Watchlist() {
    }

    public Watchlist(Usuario usuario, Filme filme) {
        this.usuario = usuario;
        this.filme = filme;
        this.dataAdicao = Instant.now();
    }

    public Watchlist(Usuario usuario, Filme filme, Instant dataAdicao) {
        this.usuario = usuario;
        this.filme = filme;
        this.dataAdicao = dataAdicao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Filme getFilme() {
        return filme;
    }

    public void setFilme(Filme filme) {
        this.filme = filme;
    }

    public Instant getDataAdicao() {
        return dataAdicao;
    }

    public void setDataAdicao(Instant dataAdicao) {
        this.dataAdicao = dataAdicao;
    }
}
