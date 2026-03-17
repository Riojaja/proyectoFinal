package pe.com.punamba.backend_punamba.dto;

import jakarta.validation.constraints.NotBlank;

public class MetodoEnvioDTO {
    private Integer idMetodoEnvio;
    @NotBlank(message = "El nombre del método de envío es obligatorio")
    private String nombre;
    private String descripcion;
    private String tiempoEstimado;
    private Boolean estado;

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