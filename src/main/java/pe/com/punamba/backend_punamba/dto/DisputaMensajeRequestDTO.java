package pe.com.punamba.backend_punamba.dto;

import jakarta.validation.constraints.NotBlank;

public class DisputaMensajeRequestDTO {

    @NotBlank(message = "El mensaje es obligatorio")
    private String mensaje;

    private String adjuntoUrl;

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getAdjuntoUrl() {
        return adjuntoUrl;
    }

    public void setAdjuntoUrl(String adjuntoUrl) {
        this.adjuntoUrl = adjuntoUrl;
    }
}