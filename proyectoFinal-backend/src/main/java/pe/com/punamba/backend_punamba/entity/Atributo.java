package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "atributos")
public class Atributo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_atributo")
    private Integer idAtributo;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "tipo_dato", length = 30)
    private String tipoDato;

    @Column(length = 20)
    private String unidad;

    public Integer getIdAtributo() {
        return idAtributo;
    }

    public void setIdAtributo(Integer idAtributo) {
        this.idAtributo = idAtributo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoDato() {
        return tipoDato;
    }

    public void setTipoDato(String tipoDato) {
        this.tipoDato = tipoDato;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }
}
