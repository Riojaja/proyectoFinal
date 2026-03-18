package pe.com.punamba.backend_punamba.dto;

import pe.com.punamba.backend_punamba.entity.DisputaMensaje;

import java.time.LocalDateTime;

public class DisputaMensajeResponseDTO {

    private Integer idMensaje;
    private Integer idDisputa;
    private Integer idUsuario;
    private String usuarioEmail;
    private String nombreCompleto;
    private DisputaMensaje.RolEmisor rolEmisor;
    private String mensaje;
    private String adjuntoUrl;
    private LocalDateTime fechaEnvio;

    public Integer getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(Integer idMensaje) {
        this.idMensaje = idMensaje;
    }

    public Integer getIdDisputa() {
        return idDisputa;
    }

    public void setIdDisputa(Integer idDisputa) {
        this.idDisputa = idDisputa;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public DisputaMensaje.RolEmisor getRolEmisor() {
        return rolEmisor;
    }

    public void setRolEmisor(DisputaMensaje.RolEmisor rolEmisor) {
        this.rolEmisor = rolEmisor;
    }

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

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }
}