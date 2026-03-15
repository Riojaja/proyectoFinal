package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendedores")
public class Vendedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vendedor")
    private Integer idVendedor;

    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Column(name = "nombre_empresa", nullable = false, length = 150)
    private String nombreTienda;

    @Column(unique = true, length = 20)
    private String ruc;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "logo")
    private String logoUrl;

    @Column(name = "calificacion_promedio", precision = 3, scale = 2)
    private BigDecimal calificacion = BigDecimal.ZERO;

    @Column(name = "total_ventas")
    private Integer totalVentas = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoVendedor estado = EstadoVendedor.pendiente;

    @Column(name = "fecha_registro", nullable = false, updatable = false, insertable = false)
    private LocalDateTime fechaRegistro;

    public enum EstadoVendedor { activo, inactivo, pendiente }

    public Vendedor() {}

    public Integer getIdVendedor() { return idVendedor; }
    public void setIdVendedor(Integer idVendedor) { this.idVendedor = idVendedor; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getNombreTienda() { return nombreTienda; }
    public void setNombreTienda(String nombreTienda) { this.nombreTienda = nombreTienda; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public BigDecimal getCalificacion() { return calificacion; }
    public void setCalificacion(BigDecimal calificacion) { this.calificacion = calificacion; }

    public Integer getTotalVentas() { return totalVentas; }
    public void setTotalVentas(Integer totalVentas) { this.totalVentas = totalVentas; }

    public EstadoVendedor getEstado() { return estado; }
    public void setEstado(EstadoVendedor estado) { this.estado = estado; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}