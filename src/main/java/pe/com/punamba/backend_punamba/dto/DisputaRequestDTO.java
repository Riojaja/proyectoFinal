package pe.com.punamba.backend_punamba.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DisputaRequestDTO {
    
    @NotNull(message = "El ID del detalle de la orden es obligatorio")
    private Integer idOrdenDetalle;

    @NotBlank(message = "El motivo de la disputa es obligatorio")
    private String motivo;

    @NotBlank(message = "Debe proporcionar una descripción del problema")
    private String descripcion;

    private String evidenciaJson;

    public Integer getIdOrdenDetalle() { return idOrdenDetalle; }
    public void setIdOrdenDetalle(Integer idOrdenDetalle) { this.idOrdenDetalle = idOrdenDetalle; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEvidenciaJson() { return evidenciaJson; }
    public void setEvidenciaJson(String evidenciaJson) { this.evidenciaJson = evidenciaJson; }
}