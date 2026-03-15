package pe.com.punamba.backend_punamba.dto;

public class CategoriaResponseDTO {

    private Integer idCategoria;
    private String nombre;
    private String descripcion;
    private String icono;
    private Boolean estado;
    
    private Integer idCategoriaPadre;
    private String nombreCategoriaPadre;

    public CategoriaResponseDTO() {}

    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }

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

    public String getNombreCategoriaPadre() { return nombreCategoriaPadre; }
    public void setNombreCategoriaPadre(String nombreCategoriaPadre) { this.nombreCategoriaPadre = nombreCategoriaPadre; }
}