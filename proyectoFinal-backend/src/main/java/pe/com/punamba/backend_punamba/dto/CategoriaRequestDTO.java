package pe.com.punamba.backend_punamba.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoriaRequestDTO {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String nombre;
    private String descripcion;
    private String icono;
    private Boolean estado = true;
    private Integer idCategoriaPadre;

    public CategoriaRequestDTO() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }

    public Integer getIdCategoriaPadre() { return idCategoriaPadre; }
    public void setIdCategoriaPadre(Integer idCategoriaPadre) { this.idCategoriaPadre = idCategoriaPadre; }
}