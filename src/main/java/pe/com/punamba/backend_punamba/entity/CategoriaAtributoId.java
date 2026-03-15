package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CategoriaAtributoId implements Serializable {

    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(name = "id_atributo")
    private Integer idAtributo;

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public Integer getIdAtributo() {
        return idAtributo;
    }

    public void setIdAtributo(Integer idAtributo) {
        this.idAtributo = idAtributo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoriaAtributoId that)) return false;
        return Objects.equals(idCategoria, that.idCategoria) &&
               Objects.equals(idAtributo, that.idAtributo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCategoria, idAtributo);
    }
}