package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "atributo_valores")
public class AtributoValor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_valor")
    private Integer idValor;

    @ManyToOne
    @JoinColumn(name = "id_atributo", nullable = false)
    private Atributo atributo;

    @Column(nullable = false, length = 80)
    private String valor;

    public Integer getIdValor() {
        return idValor;
    }

    public void setIdValor(Integer idValor) {
        this.idValor = idValor;
    }

    public Atributo getAtributo() {
        return atributo;
    }

    public void setAtributo(Atributo atributo) {
        this.atributo = atributo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}