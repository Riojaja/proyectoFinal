package pe.com.punamba.backend_punamba.dto;

import jakarta.validation.constraints.NotBlank;

public class DireccionRequestDTO {

    @NotBlank(message = "El nombre del destinatario es obligatorio")
    private String nombreDestinatario;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotBlank(message = "La dirección de envío es obligatoria")
    private String direccionLinea1;

    private String direccionLinea2;

    @NotBlank(message = "El código de ubigeo es obligatorio")
    private String codigoUbigeo;

    private Boolean esPrincipal;

    public String getNombreDestinatario() { return nombreDestinatario; }
    public void setNombreDestinatario(String nombreDestinatario) { this.nombreDestinatario = nombreDestinatario; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccionLinea1() { return direccionLinea1; }
    public void setDireccionLinea1(String direccionLinea1) { this.direccionLinea1 = direccionLinea1; }
    public String getDireccionLinea2() { return direccionLinea2; }
    public void setDireccionLinea2(String direccionLinea2) { this.direccionLinea2 = direccionLinea2; }
    public String getCodigoUbigeo() { return codigoUbigeo; }
    public void setCodigoUbigeo(String codigoUbigeo) { this.codigoUbigeo = codigoUbigeo; }
    public Boolean getEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(Boolean esPrincipal) { this.esPrincipal = esPrincipal; }
}