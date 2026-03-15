package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categoria_atributos")
public class CategoriaAtributo {

    @EmbeddedId
    private CategoriaAtributoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idCategoria")
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idAtributo")
    @JoinColumn(name = "id_atributo")
    private Atributo atributo;

    @Column(nullable = false)
    private Boolean obligatorio = false;

    @Column(nullable = false)
    private Integer orden = 0;

    public CategoriaAtributoId getId() {
        return id;
    }

    public void setId(CategoriaAtributoId id) {
        this.id = id;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Atributo getAtributo() {
        return atributo;
    }

    public void setAtributo(Atributo atributo) {
        this.atributo = atributo;
    }

    public Boolean getObligatorio() {
        return obligatorio;
    }

    public void setObligatorio(Boolean obligatorio) {
        this.obligatorio = obligatorio;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }
}