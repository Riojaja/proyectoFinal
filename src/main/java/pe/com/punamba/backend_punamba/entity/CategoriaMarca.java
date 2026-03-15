package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categoria_marca")
public class CategoriaMarca {

    @EmbeddedId
    private CategoriaMarcaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idCategoria")
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idMarca")
    @JoinColumn(name = "id_marca")
    private Marca marca;

    public CategoriaMarcaId getId() {
        return id;
    }

    public void setId(CategoriaMarcaId id) {
        this.id = id;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }
}