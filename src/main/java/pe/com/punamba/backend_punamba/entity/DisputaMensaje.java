package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Table(name = "disputa_mensajes")
public class DisputaMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Integer idMensaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_disputa", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Disputa disputa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "roles", "hibernateLazyInitializer", "handler"})
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_emisor", nullable = false)
    private RolEmisor rolEmisor;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensaje;

    @Column(name = "adjunto_url")
    private String adjuntoUrl;

    @Column(name = "fecha_envio", nullable = false, updatable = false)
    private LocalDateTime fechaEnvio;

    public enum RolEmisor {
        cliente, vendedor, admin
    }

    @PrePersist
    protected void onCreate() {
        if (this.fechaEnvio == null) {
            this.fechaEnvio = LocalDateTime.now();
        }
    }

    public DisputaMensaje() {}

    public Integer getIdMensaje() { return idMensaje; }
    public void setIdMensaje(Integer idMensaje) { this.idMensaje = idMensaje; }

    public Disputa getDisputa() { return disputa; }
    public void setDisputa(Disputa disputa) { this.disputa = disputa; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public RolEmisor getRolEmisor() { return rolEmisor; }
    public void setRolEmisor(RolEmisor rolEmisor) { this.rolEmisor = rolEmisor; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getAdjuntoUrl() { return adjuntoUrl; }
    public void setAdjuntoUrl(String adjuntoUrl) { this.adjuntoUrl = adjuntoUrl; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
}