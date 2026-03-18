package pe.com.punamba.backend_punamba.dto;

import pe.com.punamba.backend_punamba.entity.Disputa.EstadoDisputa;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DisputaResponseDTO {

    private Integer idDisputa;
    private Integer idOrden;
    private String numeroOrden;
    private Integer idOrdenDetalle;
    private String sku;
    private String usuarioEmail;
    private String clienteNombre;
    private String vendedorNombre;
    private String vendedorRuc;
    private String nombreProducto;
    private String motivo;
    private String descripcion;
    private String evidenciaJson;
    private EstadoDisputa estado;
    private BigDecimal montoReembolso;
    private String observacionAdmin;
    private String resolucionFinal;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaResolucion;
    private LocalDateTime fechaCierre;

    public Integer getIdDisputa() {
        return idDisputa;
    }

    public void setIdDisputa(Integer idDisputa) {
        this.idDisputa = idDisputa;
    }

    public Integer getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(Integer idOrden) {
        this.idOrden = idOrden;
    }

    public String getNumeroOrden() {
        return numeroOrden;
    }

    public void setNumeroOrden(String numeroOrden) {
        this.numeroOrden = numeroOrden;
    }

    public Integer getIdOrdenDetalle() {
        return idOrdenDetalle;
    }

    public void setIdOrdenDetalle(Integer idOrdenDetalle) {
        this.idOrdenDetalle = idOrdenDetalle;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getVendedorNombre() {
        return vendedorNombre;
    }

    public void setVendedorNombre(String vendedorNombre) {
        this.vendedorNombre = vendedorNombre;
    }

    public String getVendedorRuc() {
        return vendedorRuc;
    }

    public void setVendedorRuc(String vendedorRuc) {
        this.vendedorRuc = vendedorRuc;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEvidenciaJson() {
        return evidenciaJson;
    }

    public void setEvidenciaJson(String evidenciaJson) {
        this.evidenciaJson = evidenciaJson;
    }

    public EstadoDisputa getEstado() {
        return estado;
    }

    public void setEstado(EstadoDisputa estado) {
        this.estado = estado;
    }

    public BigDecimal getMontoReembolso() {
        return montoReembolso;
    }

    public void setMontoReembolso(BigDecimal montoReembolso) {
        this.montoReembolso = montoReembolso;
    }

    public String getObservacionAdmin() {
        return observacionAdmin;
    }

    public void setObservacionAdmin(String observacionAdmin) {
        this.observacionAdmin = observacionAdmin;
    }

    public String getResolucionFinal() {
        return resolucionFinal;
    }

    public void setResolucionFinal(String resolucionFinal) {
        this.resolucionFinal = resolucionFinal;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(LocalDateTime fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }
}