package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "liquidaciones")
public class Liquidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_liquidacion")
    private Integer idLiquidacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendedor", nullable = false)
    @JsonIgnoreProperties({"usuario", "hibernateLazyInitializer", "handler"})
    private Vendedor vendedor;

    @NotNull(message = "El monto total de ventas es obligatorio")
    @Column(name = "monto_total_ventas", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoTotalVentas;

    @NotNull(message = "La comisión retenida es obligatoria")
    @Column(name = "comision_retenida", precision = 10, scale = 2, nullable = false)
    private BigDecimal comisionRetenida;

    @NotNull(message = "El monto a pagar es obligatorio")
    @Column(name = "monto_a_pagar", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoAPagar;

    @Enumerated(EnumType.STRING)
    private EstadoLiquidacion estado = EstadoLiquidacion.pendiente;

    @NotNull(message = "La fecha de corte es obligatoria")
    @Column(name = "fecha_corte", nullable = false)
    private LocalDate fechaCorte;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "comprobante_transferencia", length = 255)
    private String comprobanteTransferencia;

    public enum EstadoLiquidacion { pendiente, procesando, pagado }

    public Liquidacion() {}

    public Integer getIdLiquidacion() { return idLiquidacion; }
    public void setIdLiquidacion(Integer idLiquidacion) { this.idLiquidacion = idLiquidacion; }

    public Vendedor getVendedor() { return vendedor; }
    public void setVendedor(Vendedor vendedor) { this.vendedor = vendedor; }

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