package pe.com.punamba.backend_punamba.dto;

import jakarta.validation.constraints.NotBlank;

public class TiendaRequestDTO {

    @NotBlank(message = "El nombre de la tienda es obligatorio")
    private String nombre;
    private String descripcion;
    private String banner;
    private String direccion;
    private String telefono;
    private String email;
    private Boolean estado;

    public TiendaRequestDTO() {}

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
}