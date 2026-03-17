package pe.com.punamba.backend_punamba.dto;

import pe.com.punamba.backend_punamba.entity.Transaccion.EstadoTransaccion;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransaccionResponseDTO {
    private Integer idTransaccion;
    private String numeroOrden;
    private String metodoPagoNombre;
    private BigDecimal monto;
    private String numeroTransaccion;
    private EstadoTransaccion estado;
    private LocalDateTime fechaTransaccion;

    public Integer getIdTransaccion() { return idTransaccion; }
    public void setIdTransaccion(Integer idTransaccion) { this.idTransaccion = idTransaccion; }
    public String getNumeroOrden() { return numeroOrden; }
    public void setNumeroOrden(String numeroOrden) { this.numeroOrden = numeroOrden; }
    public String getMetodoPagoNombre() { return metodoPagoNombre; }
    public void setMetodoPagoNombre(String metodoPagoNombre) { this.metodoPagoNombre = metodoPagoNombre; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getNumeroTransaccion() { return numeroTransaccion; }
    public void setNumeroTransaccion(String numeroTransaccion) { this.numeroTransaccion = numeroTransaccion; }
    public EstadoTransaccion getEstado() { return estado; }
    public void setEstado(EstadoTransaccion estado) { this.estado = estado; }
    public LocalDateTime getFechaTransaccion() { return fechaTransaccion; }
    public void setFechaTransaccion(LocalDateTime fechaTransaccion) { this.fechaTransaccion = fechaTransaccion; }
}