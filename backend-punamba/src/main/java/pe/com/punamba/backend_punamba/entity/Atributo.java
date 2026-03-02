package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Importación necesaria
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "atributos")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Atributo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_atributo")
    private Integer idAtributo;

    @NotBlank(message = "El nombre del atributo es obligatorio")
    @Column(unique = true, nullable = false, length = 50)
    private String nombre;

    @OneToMany(mappedBy = "atributo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "atributo-valor")
    private List<AtributoValor> valores = new ArrayList<>();

    public Atributo() {}

    public Integer getIdAtributo() { return idAtributo; }
    public void setIdAtributo(Integer idAtributo) { this.idAtributo = idAtributo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<AtributoValor> getValores() { return valores; }
    public void setValores(List<AtributoValor> valores) { 
        this.valores = valores; 
        if(valores != null) {
            for(AtributoValor valor : valores) {
                valor.setAtributo(this);
            }
        }
    }
}