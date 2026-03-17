package pe.com.punamba.backend_punamba.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class ProductoRequestDTO {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;
    
    private String descripcion;
    private String marca;
    private String modelo;
    private BigDecimal precioBase;

    @NotNull(message = "Debe seleccionar una categoría")
    private Integer idCategoria;

    private List<VarianteRequestDTO> variantes;

    public ProductoRequestDTO() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public BigDecimal getPrecioBase() { return precioBase; }
    public void setPrecioBase(BigDecimal precioBase) { this.precioBase = precioBase; }

    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }

    public List<VarianteRequestDTO> getVariantes() { return variantes; }
    public void setVariantes(List<VarianteRequestDTO> variantes) { this.variantes = variantes; }
}