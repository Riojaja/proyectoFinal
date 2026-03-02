package pe.com.punamba.backend_punamba.dto;

import java.math.BigDecimal;
import java.util.List;

public class VarianteResponseDTO {

    private Integer idVariante;
    private String sku;
    private BigDecimal precio;
    private BigDecimal precioOferta;
    private Integer stock;
    private Boolean activo;
    private List<VarianteAtributoResponseDTO> atributos;

    public VarianteResponseDTO() {}

    public Integer getIdVariante() { return idVariante; }
    public void setIdVariante(Integer idVariante) { this.idVariante = idVariante; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public BigDecimal getPrecioOferta() { return precioOferta; }
    public void setPrecioOferta(BigDecimal precioOferta) { this.precioOferta = precioOferta; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public List<VarianteAtributoResponseDTO> getAtributos() { return atributos; }
    public void setAtributos(List<VarianteAtributoResponseDTO> atributos) { this.atributos = atributos; }
}