package br.ufpb.dsc.mercado.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "filme")
public class Filme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título do filme é obrigatório")
    @Size(max = 150, message = "O título do filme não pode exceder 150 caracteres")
    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Size(max = 100, message = "O nome do diretor não pode exceder 100 caracteres")
    @Column(name = "diretor", length = 100)
    private String diretor;

    @NotNull(message = "O ano de lançamento é obrigatório")
    @Column(name = "ano", nullable = false)
    private Integer ano;

    @Column(name = "sinopse", columnDefinition = "TEXT")
    private String sinopse;

    @Size(max = 50, message = "O gênero não pode exceder 50 caracteres")
    @Column(name = "genero", length = 50)
    private String genero;

    @Size(max = 500, message = "A URL da imagem não pode exceder 500 caracteres")
    @Column(name = "imagem_url", length = 500)
    private String imagemUrl;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @PrePersist
    protected void prePersist() {
        Instant agora = Instant.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    @PreUpdate
    protected void preUpdate() {
        this.atualizadoEm = Instant.now();
    }

    public Filme() {
    }

    public Filme(String titulo, String diretor, Integer ano, String sinopse, String genero, String imagemUrl) {
        this.titulo = titulo;
        this.diretor = diretor;
        this.ano = ano;
        this.sinopse = sinopse;
        this.genero = genero;
        this.imagemUrl = imagemUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDiretor() {
        return diretor;
    }

    public void setDiretor(String diretor) {
        this.diretor = diretor;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getSinopse() {
        return sinopse;
    }

    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(Instant atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
