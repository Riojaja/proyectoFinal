package pe.com.punamba.backend_punamba.dto;

import pe.com.punamba.backend_punamba.entity.Liquidacion.EstadoLiquidacion;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LiquidacionResponseDTO {
    private Integer idLiquidacion;
    private Integer idVendedor;
    private String nombreTienda;
    private BigDecimal montoTotalVentas;
    private BigDecimal comisionRetenida;
    private BigDecimal montoAPagar;
    private EstadoLiquidacion estado;
    private LocalDate fechaCorte;
    private LocalDateTime fechaPago;
    private String comprobanteTransferencia;

    public Integer getIdLiquidacion() { return idLiquidacion; }
    public void setIdLiquidacion(Integer idLiquidacion) { this.idLiquidacion = idLiquidacion; }
    public Integer getIdVendedor() { return idVendedor; }
    public void setIdVendedor(Integer idVendedor) { this.idVendedor = idVendedor; }
    public String getNombreTienda() { return nombreTienda; }
    public void setNombreTienda(String nombreTienda) { this.nombreTienda = nombreTienda; }
    public BigDecimal getMontoTotalVentas() { return montoTotalVentas; }
    public void setMontoTotalVentas(BigDecimal montoTotalVentas) { this.montoTotalVentas = montoTotalVentas; }
    public BigDecimal getComisionRetenida() { return comisionRetenida; }
    public void setComisionRetenida(BigDecimal comisionRetenida) { this.comisionRetenida = comisionRetenida; }
    public BigDecimal getMontoAPagar() { return montoAPagar; }
    public void setMontoAPagar(BigDecimal montoAPagar) { this.montoAPagar = montoAPagar; }
    public EstadoLiquidacion getEstado() { return estado; }
    public void setEstado(EstadoLiquidacion estado) { this.estado = estado; }
    public LocalDate getFechaCorte() { return fechaCorte; }
    public void setFechaCorte(LocalDate fechaCorte) { this.fechaCorte = fechaCorte; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
    public String getComprobanteTransferencia() { return comprobanteTransferencia; }
    public void setComprobanteTransferencia(String comprobanteTransferencia) { this.comprobanteTransferencia = comprobanteTransferencia; }
}