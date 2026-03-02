package pe.com.punamba.backend_punamba.dto;

import pe.com.punamba.backend_punamba.entity.Cupon.TipoDescuento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CuponResponseDTO {
    private Integer idCupon;
    private Integer idVendedor;
    private String codigo;
    private String descripcion;
    private TipoDescuento tipoDescuento;
    private BigDecimal valorDescuento;
    private BigDecimal montoMinimo;
    private Integer cantidadMaximaUsos;
    private Integer cantidadUsos;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaExpiracion;
    private Boolean activo;

    public Integer getIdCupon() { return idCupon; }
    public void setIdCupon(Integer idCupon) { this.idCupon = idCupon; }
    public Integer getIdVendedor() { return idVendedor; }
    public void setIdVendedor(Integer idVendedor) { this.idVendedor = idVendedor; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public TipoDescuento getTipoDescuento() { return tipoDescuento; }
    public void setTipoDescuento(TipoDescuento tipoDescuento) { this.tipoDescuento = tipoDescuento; }
    public BigDecimal getValorDescuento() { return valorDescuento; }
    public void setValorDescuento(BigDecimal valorDescuento) { this.valorDescuento = valorDescuento; }
    public BigDecimal getMontoMinimo() { return montoMinimo; }
    public void setMontoMinimo(BigDecimal montoMinimo) { this.montoMinimo = montoMinimo; }
    public Integer getCantidadMaximaUsos() { return cantidadMaximaUsos; }
    public void setCantidadMaximaUsos(Integer cantidadMaximaUsos) { this.cantidadMaximaUsos = cantidadMaximaUsos; }
    public Integer getCantidadUsos() { return cantidadUsos; }
    public void setCantidadUsos(Integer cantidadUsos) { this.cantidadUsos = cantidadUsos; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}