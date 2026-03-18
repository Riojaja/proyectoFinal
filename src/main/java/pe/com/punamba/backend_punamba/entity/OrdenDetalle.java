package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "ordenes_detalle")
public class OrdenDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_detalle")
    private Integer idOrdenDetalle;

    @ManyToOne
    @JoinColumn(name = "id_orden", nullable = false)
    @JsonBackReference(value = "orden-detalle")
    private Orden orden;

    @ManyToOne
    @JoinColumn(name = "id_variante", nullable = false)
    private ProductoVariante variante;

    @ManyToOne
    @JoinColumn(name = "id_vendedor", nullable = false)
    private Vendedor vendedor;

    @ManyToOne
    @JoinColumn(name = "id_metodo_envio", nullable = false)
    private MetodoEnvio metodoEnvio;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario_snapshot", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitarioSnapshot;

    @Column(name = "comision_plataforma", precision = 10, scale = 2)
    private BigDecimal comisionPlataforma;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "estado_envio", length = 50)
    private String estadoEnvio;

    @Column(name = "numero_seguimiento", length = 100)
    private String numeroSeguimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_postventa", nullable = false)
    private EstadoPostventa estadoPostventa = EstadoPostventa.normal;

    @Column(name = "monto_reembolsado", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoReembolsado = BigDecimal.ZERO;

    public enum EstadoPostventa {
        normal, en_disputa, reembolsado, rechazado
    }

    public OrdenDetalle() {}

    public Integer getIdOrdenDetalle() { return idOrdenDetalle; }
    public void setIdOrdenDetalle(Integer idOrdenDetalle) { this.idOrdenDetalle = idOrdenDetalle; }

    public Orden getOrden() { return orden; }
    public void setOrden(Orden orden) { this.orden = orden; }

    public ProductoVariante getVariante() { return variante; }
    public void setVariante(ProductoVariante variante) { this.variante = variante; }

    public Vendedor getVendedor() { return vendedor; }
    public void setVendedor(Vendedor vendedor) { this.vendedor = vendedor; }

    public MetodoEnvio getMetodoEnvio() { return metodoEnvio; }
    public void setMetodoEnvio(MetodoEnvio metodoEnvio) { this.metodoEnvio = metodoEnvio; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitarioSnapshot() { return precioUnitarioSnapshot; }
    public void setPrecioUnitarioSnapshot(BigDecimal precioUnitarioSnapshot) { this.precioUnitarioSnapshot = precioUnitarioSnapshot; }

    public BigDecimal getComisionPlataforma() { return comisionPlataforma; }
    public void setComisionPlataforma(BigDecimal comisionPlataforma) { this.comisionPlataforma = comisionPlataforma; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public String getEstadoEnvio() { return estadoEnvio; }
    public void setEstadoEnvio(String estadoEnvio) { this.estadoEnvio = estadoEnvio; }

    public String getNumeroSeguimiento() { return numeroSeguimiento; }
    public void setNumeroSeguimiento(String numeroSeguimiento) { this.numeroSeguimiento = numeroSeguimiento; }

    public EstadoPostventa getEstadoPostventa() { return estadoPostventa; }
    public void setEstadoPostventa(EstadoPostventa estadoPostventa) { this.estadoPostventa = estadoPostventa; }

    public BigDecimal getMontoReembolsado() { return montoReembolsado; }
    public void setMontoReembolsado(BigDecimal montoReembolsado) { this.montoReembolsado = montoReembolsado; }
}