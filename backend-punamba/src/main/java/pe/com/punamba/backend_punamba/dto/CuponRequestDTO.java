package pe.com.punamba.backend_punamba.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pe.com.punamba.backend_punamba.entity.Cupon.TipoDescuento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CuponRequestDTO {
    @NotBlank(message = "El código del cupón es obligatorio")
    private String codigo;
    private String descripcion;
    @NotNull(message = "Debe especificar el tipo de descuento")
    private TipoDescuento tipoDescuento;
    @NotNull(message = "El valor del descuento es obligatorio")
    private BigDecimal valorDescuento;
    private BigDecimal montoMinimo;
    private Integer cantidadMaximaUsos;
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime fechaInicio;
    @NotNull(message = "La fecha de expiración es obligatoria")
    private LocalDateTime fechaExpiracion;
    private Boolean activo;

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
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}