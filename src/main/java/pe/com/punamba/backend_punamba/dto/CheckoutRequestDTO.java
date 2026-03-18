package pe.com.punamba.backend_punamba.dto;

import java.util.List;

public class CheckoutRequestDTO {

    private Integer idDireccion;
    private String notas;
    private List<CheckoutItemRequestDTO> items;

    public Integer getIdDireccion() {
        return idDireccion;
    }

    public void setIdDireccion(Integer idDireccion) {
        this.idDireccion = idDireccion;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public List<CheckoutItemRequestDTO> getItems() {
        return items;
    }

    public void setItems(List<CheckoutItemRequestDTO> items) {
        this.items = items;
    }
}