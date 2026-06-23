package br.ufpb.dsc.mercado.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "diario_filme", uniqueConstraints = {
    @UniqueConstraint(name = "uk_usuario_filme_diario", columnNames = {"usuario_id", "filme_id"})
})
public class DiarioFilme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filme_id", nullable = false)
    private Filme filme;

    @Column(name = "data_assistido", nullable = false)
    private Instant dataAssistido;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @PrePersist
    protected void prePersist() {
        if (this.dataAssistido == null) {
            this.dataAssistido = Instant.now();
        }
    }

    public DiarioFilme() {
    }

    public DiarioFilme(Usuario usuario, Filme filme, Instant dataAssistido, String observacao) {
        this.usuario = usuario;
        this.filme = filme;
        this.dataAssistido = dataAssistido;
        this.observacao = observacao;
    }

    public DiarioFilme(Usuario usuario, Filme filme, String observacao) {
        this.usuario = usuario;
        this.filme = filme;
        this.dataAssistido = Instant.now();
        this.observacao = observacao;
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

    public Instant getDataAssistido() {
        return dataAssistido;
    }

    public void setDataAssistido(Instant dataAssistido) {
        this.dataAssistido = dataAssistido;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
