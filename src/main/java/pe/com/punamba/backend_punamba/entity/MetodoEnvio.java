package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "metodos_envio")
public class MetodoEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo_envio")
    private Integer idMetodoEnvio;

    @NotBlank(message = "El nombre del método de envío es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "tiempo_estimado", length = 50)
    private String tiempoEstimado;

    private Boolean estado = true;

    public MetodoEnvio() {}

    public Integer getIdMetodoEnvio() { return idMetodoEnvio; }
    public void setIdMetodoEnvio(Integer idMetodoEnvio) { this.idMetodoEnvio = idMetodoEnvio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTiempoEstimado() { return tiempoEstimado; }
    public void setTiempoEstimado(String tiempoEstimado) { this.tiempoEstimado = tiempoEstimado; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }
}