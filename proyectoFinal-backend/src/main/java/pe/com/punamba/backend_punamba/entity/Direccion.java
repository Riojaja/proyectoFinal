package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "direcciones_envio")
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion")
    private Integer idDireccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    @NotBlank(message = "El nombre del destinatario es obligatorio")
    @Column(name = "nombre_destinatario", nullable = false, length = 150)
    private String nombreDestinatario;

    @NotBlank(message = "El teléfono es obligatorio")
    @Column(nullable = false, length = 20)
    private String telefono;

    @NotBlank(message = "La dirección de envío es obligatoria")
    @Column(name = "direccion_linea1", nullable = false)
    private String direccionLinea1;

    @Column(name = "direccion_linea2")
    private String direccionLinea2;

    @ManyToOne
    @JoinColumn(name = "codigo_ubigeo", nullable = false)
    private Ubigeo ubigeo;

    @Column(name = "es_principal")
    private Boolean esPrincipal = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false, insertable = false)
    private LocalDateTime fechaCreacion;

    public Direccion() {}

    public Integer getIdDireccion() { return idDireccion; }
    public void setIdDireccion(Integer idDireccion) { this.idDireccion = idDireccion; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getNombreDestinatario() { return nombreDestinatario; }
    public void setNombreDestinatario(String nombreDestinatario) { this.nombreDestinatario = nombreDestinatario; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccionLinea1() { return direccionLinea1; }
    public void setDireccionLinea1(String direccionLinea1) { this.direccionLinea1 = direccionLinea1; }
    public String getDireccionLinea2() { return direccionLinea2; }
    public void setDireccionLinea2(String direccionLinea2) { this.direccionLinea2 = direccionLinea2; }
    public Ubigeo getUbigeo() { return ubigeo; }
    public void setUbigeo(Ubigeo ubigeo) { this.ubigeo = ubigeo; }
    public Boolean getEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(Boolean esPrincipal) { this.esPrincipal = esPrincipal; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}