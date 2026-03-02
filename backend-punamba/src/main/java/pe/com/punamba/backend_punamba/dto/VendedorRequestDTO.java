package pe.com.punamba.backend_punamba.dto;

import jakarta.validation.constraints.NotBlank;

public class VendedorRequestDTO {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    private String nombreTienda;
    private String ruc;
    private String descripcion;
    private String logoUrl;

    public VendedorRequestDTO() {}

    public String getNombreTienda() { return nombreTienda; }
    public void setNombreTienda(String nombreTienda) { this.nombreTienda = nombreTienda; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
}