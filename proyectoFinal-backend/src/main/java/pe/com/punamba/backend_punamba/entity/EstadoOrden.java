package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "estados_orden")
public class EstadoOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Integer idEstado;

    @NotBlank(message = "El nombre del estado es obligatorio")
    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    private String descripcion;

    @Column(length = 20)
    private String color;

    public EstadoOrden() {}

    public Integer getIdEstado() { return idEstado; }
    public void setIdEstado(Integer idEstado) { this.idEstado = idEstado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}