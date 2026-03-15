package pe.com.punamba.backend_punamba.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class VarianteRequestDTO {

    @NotBlank(message = "El SKU es obligatorio")
    private String sku;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin("0.0")
    private BigDecimal precio;

    private BigDecimal precioOferta;

    @NotNull(message = "El stock es obligatorio")
    @Min(0)
    private Integer stock;

    private List<VarianteAtributoRequestDTO> atributos;

    public VarianteRequestDTO() {}

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public BigDecimal getPrecioOferta() { return precioOferta; }
    public void setPrecioOferta(BigDecimal precioOferta) { this.precioOferta = precioOferta; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public List<VarianteAtributoRequestDTO> getAtributos() { return atributos; }
    public void setAtributos(List<VarianteAtributoRequestDTO> atributos) { this.atributos = atributos; }
}