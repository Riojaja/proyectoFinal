package pe.com.punamba.backend_punamba.dto;

import java.math.BigDecimal;

public class OrdenDetalleResponseDTO {
    private Integer idOrdenDetalle;
    private Integer idVariante;
    private String nombreProducto;
    private String nombreVendedor;
    private String metodoEnvio;
    private Integer cantidad;
    private BigDecimal precioUnitarioSnapshot;
    private BigDecimal subtotal;
    private String estadoEnvio;
    private String numeroSeguimiento;
    private String imagen;
    private String estadoPostventa;
    private BigDecimal montoReembolsado;

    public Integer getIdOrdenDetalle() {
        return idOrdenDetalle;
    }

    public void setIdOrdenDetalle(Integer idOrdenDetalle) {
        this.idOrdenDetalle = idOrdenDetalle;
    }

    public Integer getIdVariante() {
        return idVariante;
    }

    public void setIdVariante(Integer idVariante) {
        this.idVariante = idVariante;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getNombreVendedor() {
        return nombreVendedor;
    }

    public void setNombreVendedor(String nombreVendedor) {
        this.nombreVendedor = nombreVendedor;
    }

    public String getMetodoEnvio() {
        return metodoEnvio;
    }

    public void setMetodoEnvio(String metodoEnvio) {
        this.metodoEnvio = metodoEnvio;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitarioSnapshot() {
        return precioUnitarioSnapshot;
    }

    public void setPrecioUnitarioSnapshot(BigDecimal precioUnitarioSnapshot) {
        this.precioUnitarioSnapshot = precioUnitarioSnapshot;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getEstadoEnvio() {
        return estadoEnvio;
    }

    public void setEstadoEnvio(String estadoEnvio) {
        this.estadoEnvio = estadoEnvio;
    }

    public String getNumeroSeguimiento() {
        return numeroSeguimiento;
    }

    public void setNumeroSeguimiento(String numeroSeguimiento) {
        this.numeroSeguimiento = numeroSeguimiento;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getEstadoPostventa() {
        return estadoPostventa;
    }

    public void setEstadoPostventa(String estadoPostventa) {
        this.estadoPostventa = estadoPostventa;
    }

    public BigDecimal getMontoReembolsado() {
        return montoReembolsado;
    }

    public void setMontoReembolsado(BigDecimal montoReembolsado) {
        this.montoReembolsado = montoReembolsado;
    }
}