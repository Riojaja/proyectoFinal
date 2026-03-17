package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Importación necesaria
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "variante_atributos")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VarianteAtributo {

    @EmbeddedId
    private VarianteAtributoId id = new VarianteAtributoId();

    @ManyToOne
    @MapsId("idVariante")
    @JoinColumn(name = "id_variante")
    @JsonBackReference(value = "variante-atributos")
    private ProductoVariante variante;

    @ManyToOne
    @MapsId("idAtributo")
    @JoinColumn(name = "id_atributo")
    private Atributo atributo;

    @ManyToOne
    @JoinColumn(name = "id_valor", nullable = false)
    private AtributoValor valor;

    public VarianteAtributo() {}

    public VarianteAtributoId getId() { return id; }
    public void setId(VarianteAtributoId id) { this.id = id; }

    public ProductoVariante getVariante() { return variante; }
    public void setVariante(ProductoVariante variante) { this.variante = variante; }

    public Atributo getAtributo() { return atributo; }
    public void setAtributo(Atributo atributo) { this.atributo = atributo; }

    public AtributoValor getValor() { return valor; }
    public void setValor(AtributoValor valor) { this.valor = valor; }

    @Embeddable
    public static class VarianteAtributoId implements Serializable {
        
        @Column(name = "id_variante")
        private Integer idVariante;
        
        @Column(name = "id_atributo")
        private Integer idAtributo;

        public VarianteAtributoId() {}

        public VarianteAtributoId(Integer idVariante, Integer idAtributo) {
            this.idVariante = idVariante;
            this.idAtributo = idAtributo;
        }

        public Integer getIdVariante() { return idVariante; }
        public Integer getIdAtributo() { return idAtributo; }

        public void setIdVariante(Integer idVariante) { this.idVariante = idVariante; }
        public void setIdAtributo(Integer idAtributo) { this.idAtributo = idAtributo; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VarianteAtributoId that = (VarianteAtributoId) o;
            return Objects.equals(idVariante, that.idVariante) && 
                   Objects.equals(idAtributo, that.idAtributo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idVariante, idAtributo);
        }
    }
}