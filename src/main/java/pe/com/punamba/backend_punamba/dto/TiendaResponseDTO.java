package pe.com.punamba.backend_punamba.dto;

import java.time.LocalDateTime;

public class TiendaResponseDTO {

    private Integer idTienda;
    private Integer idVendedor;
    private String nombre;
    private String descripcion;
    private String banner;
    private String direccion;
    private String telefono;
    private String email;
    private Boolean estado;
    private LocalDateTime fechaCreacion;

    public TiendaResponseDTO() {}

    public Integer getIdTienda() { return idTienda; }
    public void setIdTienda(Integer idTienda) { this.idTienda = idTienda; }
    public Integer getIdVendedor() { return idVendedor; }
    public void setIdVendedor(Integer idVendedor) { this.idVendedor = idVendedor; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getBanner() { return banner; }
    public void setBanner(String banner) { this.banner = banner; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}