package pe.com.punamba.backend_punamba.dto;

import pe.com.punamba.backend_punamba.entity.Vendedor.EstadoVendedor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VendedorResponseDTO {

    private Integer idVendedor;
    private String nombreTienda;
    private String ruc;
    private String descripcion;
    private String logoUrl;
    private BigDecimal calificacion;
    private Integer totalVentas;
    private EstadoVendedor estado;
    private LocalDateTime fechaRegistro;
    
    private Integer idUsuario;
    private String nombreUsuario;

    public VendedorResponseDTO() {}

    public Integer getIdVendedor() { return idVendedor; }
    public void setIdVendedor(Integer idVendedor) { this.idVendedor = idVendedor; }
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
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
}