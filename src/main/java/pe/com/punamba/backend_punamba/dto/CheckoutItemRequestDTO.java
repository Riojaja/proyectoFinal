package pe.com.punamba.backend_punamba.dto;

public class CheckoutItemRequestDTO {

    private Integer idVariante;
    private Integer cantidad;

    public Integer getIdVariante() {
        return idVariante;
    }

    public void setIdVariante(Integer idVariante) {
        this.idVariante = idVariante;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}