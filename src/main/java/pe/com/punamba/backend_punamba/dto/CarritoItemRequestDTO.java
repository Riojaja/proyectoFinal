package pe.com.punamba.backend_punamba.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CarritoItemRequestDTO {
    @NotNull(message = "El ID de la variante es obligatorio")
    private Integer idVariante;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;

    public Integer getIdVariante() { return idVariante; }
    public void setIdVariante(Integer idVariante) { this.idVariante = idVariante; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}