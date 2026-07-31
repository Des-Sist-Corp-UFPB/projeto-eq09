package br.ufpb.dsc.mercado.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Entity
@Table(name = "comentario")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filme_id", nullable = false)
    private Filme filme;

    @NotBlank(message = "O texto do comentário não pode estar vazio")
    @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(name = "contains_spoiler")
    private Boolean containsSpoiler = false;

    @Column(name = "spoiler_confidence")
    private Double spoilerConfidence;

    @Column(name = "spoiler_level")
    private String spoilerLevel;

    @Column(name = "spoiler_segments", columnDefinition = "TEXT")
    private String spoilerSegments;

    @Column(name = "spoiler_checked_at")
    private Instant spoilerCheckedAt;

    @Column(name = "spoiler_model_version")
    private String spoilerModelVersion;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    protected void prePersist() {
        this.criadoEm = Instant.now();
    }

    public Comentario() {
    }

    public Comentario(Usuario usuario, Filme filme, String texto) {
        this.usuario = usuario;
        this.filme = filme;
        this.texto = texto;
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

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Boolean getContainsSpoiler() {
        return containsSpoiler;
    }

    public void setContainsSpoiler(Boolean containsSpoiler) {
        this.containsSpoiler = containsSpoiler;
    }

    public Double getSpoilerConfidence() {
        return spoilerConfidence;
    }

    public void setSpoilerConfidence(Double spoilerConfidence) {
        this.spoilerConfidence = spoilerConfidence;
    }

    public String getSpoilerLevel() {
        return spoilerLevel;
    }

    public void setSpoilerLevel(String spoilerLevel) {
        this.spoilerLevel = spoilerLevel;
    }

    public String getSpoilerSegments() {
        return spoilerSegments;
    }

    public void setSpoilerSegments(String spoilerSegments) {
        this.spoilerSegments = spoilerSegments;
    }

    public Instant getSpoilerCheckedAt() {
        return spoilerCheckedAt;
    }

    public void setSpoilerCheckedAt(Instant spoilerCheckedAt) {
        this.spoilerCheckedAt = spoilerCheckedAt;
    }

    public String getSpoilerModelVersion() {
        return spoilerModelVersion;
    }

    public void setSpoilerModelVersion(String spoilerModelVersion) {
        this.spoilerModelVersion = spoilerModelVersion;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }
}
