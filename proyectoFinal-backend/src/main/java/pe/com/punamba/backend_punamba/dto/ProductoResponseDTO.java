package pe.com.punamba.backend_punamba.dto;

import pe.com.punamba.backend_punamba.entity.Producto.EstadoProducto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ProductoResponseDTO {

    private Integer idProducto;
    private String nombre;
    private String descripcion;
    private String marca;
    private String modelo;
    private BigDecimal precioBase;
    private EstadoProducto estado;
    private LocalDateTime fechaCreacion;

    private Integer idCategoria;
    private String nombreCategoria;

    private Integer idVendedor;
    private String nombreTienda;

    private List<ProductoImagenDTO> imagenes;
    private List<VarianteResponseDTO> variantes;

    public ProductoResponseDTO() {}

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public EstadoProducto getEstado() {
        return estado;
    }

    public void setEstado(EstadoProducto estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public Integer getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(Integer idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getNombreTienda() {
        return nombreTienda;
    }

    public void setNombreTienda(String nombreTienda) {
        this.nombreTienda = nombreTienda;
    }

    public List<ProductoImagenDTO> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<ProductoImagenDTO> imagenes) {
        this.imagenes = imagenes;
    }

    public List<VarianteResponseDTO> getVariantes() {
        return variantes;
    }

    public void setVariantes(List<VarianteResponseDTO> variantes) {
        this.variantes = variantes;
    }
}