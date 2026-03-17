package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones")
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaccion")
    private Integer idTransaccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Evita bucles o carga excesiva de la orden
    private Orden orden;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    @NotNull(message = "El monto es obligatorio")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "numero_transaccion", unique = true, length = 100)
    private String numeroTransaccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTransaccion estado = EstadoTransaccion.pendiente;

    @Column(name = "detalles_pago", columnDefinition = "JSON")
    private String detallesPago;

    @Column(name = "fecha_transaccion", nullable = false, updatable = false, insertable = false)
    private LocalDateTime fechaTransaccion;

    public enum EstadoTransaccion { pendiente, completado, fallido, reembolsado }

    public Transaccion() {}

    public Integer getIdTransaccion() { return idTransaccion; }
    public void setIdTransaccion(Integer idTransaccion) { this.idTransaccion = idTransaccion; }
    
    public Orden getOrden() { return orden; }
    public void setOrden(Orden orden) { this.orden = orden; }
    
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
    
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    
    public String getNumeroTransaccion() { return numeroTransaccion; }
    public void setNumeroTransaccion(String numeroTransaccion) { this.numeroTransaccion = numeroTransaccion; }
    
    public EstadoTransaccion getEstado() { return estado; }
    public void setEstado(EstadoTransaccion estado) { this.estado = estado; }
    
    public String getDetallesPago() { return detallesPago; }
    public void setDetallesPago(String detallesPago) { this.detallesPago = detallesPago; }
    
    public LocalDateTime getFechaTransaccion() { return fechaTransaccion; }
    public void setFechaTransaccion(LocalDateTime fechaTransaccion) { this.fechaTransaccion = fechaTransaccion; }
}