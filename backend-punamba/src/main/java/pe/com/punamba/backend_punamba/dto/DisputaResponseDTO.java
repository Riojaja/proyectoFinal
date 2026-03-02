package pe.com.punamba.backend_punamba.dto;

import pe.com.punamba.backend_punamba.entity.Disputa.EstadoDisputa;
import java.time.LocalDateTime;

public class DisputaResponseDTO {
    private Integer idDisputa;
    private Integer idOrdenDetalle;
    private String nombreProducto;
    private String motivo;
    private String descripcion;
    private String evidenciaJson;
    private EstadoDisputa estado;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaResolucion;

    public Integer getIdDisputa() { return idDisputa; }
    public void setIdDisputa(Integer idDisputa) { this.idDisputa = idDisputa; }
    public Integer getIdOrdenDetalle() { return idOrdenDetalle; }
    public void setIdOrdenDetalle(Integer idOrdenDetalle) { this.idOrdenDetalle = idOrdenDetalle; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEvidenciaJson() { return evidenciaJson; }
    public void setEvidenciaJson(String evidenciaJson) { this.evidenciaJson = evidenciaJson; }
    public EstadoDisputa getEstado() { return estado; }
    public void setEstado(EstadoDisputa estado) { this.estado = estado; }
    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }
    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }
}