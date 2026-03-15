package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CategoriaMarcaId implements Serializable {

    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(name = "id_marca")
    private Integer idMarca;

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public Integer getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(Integer idMarca) {
        this.idMarca = idMarca;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CategoriaMarcaId that))
            return false;
        return Objects.equals(idCategoria, that.idCategoria) &&
                Objects.equals(idMarca, that.idMarca);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCategoria, idMarca);
    }
}