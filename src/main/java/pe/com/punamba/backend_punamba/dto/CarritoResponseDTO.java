package pe.com.punamba.backend_punamba.dto;

import java.math.BigDecimal;
import java.util.List;

public class CarritoResponseDTO {
    private Integer idCarrito;
    private Integer idUsuario;
    private List<CarritoItemResponseDTO> items;
    private BigDecimal totalCarrito;

    public Integer getIdCarrito() { return idCarrito; }
    public void setIdCarrito(Integer idCarrito) { this.idCarrito = idCarrito; }
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public List<CarritoItemResponseDTO> getItems() { return items; }
    public void setItems(List<CarritoItemResponseDTO> items) { this.items = items; }
    public BigDecimal getTotalCarrito() { return totalCarrito; }
    public void setTotalCarrito(BigDecimal totalCarrito) { this.totalCarrito = totalCarrito; }
}