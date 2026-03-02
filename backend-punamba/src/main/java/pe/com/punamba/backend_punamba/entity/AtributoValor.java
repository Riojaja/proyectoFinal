package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Importación necesaria

@Entity
@Table(name = "atributo_valores", uniqueConstraints = {@UniqueConstraint(columnNames = {"id_atributo", "valor"})})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AtributoValor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_valor")
    private Integer idValor;

    @ManyToOne
    @JoinColumn(name = "id_atributo", nullable = false)
    @JsonBackReference(value = "atributo-valor")
    private Atributo atributo;

    @NotBlank(message = "El valor es obligatorio")
    @Column(nullable = false, length = 80)
    private String valor;

    public AtributoValor() {}

    public Integer getIdValor() { return idValor; }
    public void setIdValor(Integer idValor) { this.idValor = idValor; }

    public Atributo getAtributo() { return atributo; }
    public void setAtributo(Atributo atributo) { this.atributo = atributo; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
}