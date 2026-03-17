package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cupones")
public class Cupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cupon")
    private Integer idCupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendedor")
    @JsonIgnoreProperties({"usuario", "hibernateLazyInitializer", "handler"})
    private Vendedor vendedor;

    @NotBlank(message = "El código del cupón es obligatorio")
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(length = 255)
    private String descripcion;

    @NotNull(message = "Debe especificar el tipo de descuento (porcentaje o monto_fijo)")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_descuento", nullable = false)
    private TipoDescuento tipoDescuento;

    @NotNull(message = "El valor del descuento es obligatorio")
    @Column(name = "valor_descuento", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorDescuento;

    @Column(name = "monto_minimo", precision = 10, scale = 2)
    private BigDecimal montoMinimo;

    @Column(name = "cantidad_maxima_usos")
    private Integer cantidadMaximaUsos;

    @Column(name = "cantidad_usos")
    private Integer cantidadUsos = 0;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de expiración es obligatoria")
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false, insertable = false)
    private LocalDateTime fechaCreacion;

    public enum TipoDescuento { porcentaje, monto_fijo }

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Cupon() {}

    public Integer getIdCupon() { return idCupon; }
    public void setIdCupon(Integer idCupon) { this.idCupon = idCupon; }

    public Vendedor getVendedor() { return vendedor; }
    public void setVendedor(Vendedor vendedor) { this.vendedor = vendedor; }

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

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}