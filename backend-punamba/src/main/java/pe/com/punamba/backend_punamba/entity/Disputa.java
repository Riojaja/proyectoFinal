package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "disputas")
public class Disputa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_disputa")
    private Integer idDisputa;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden_detalle", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "orden"})
    private OrdenDetalle ordenDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "roles", "hibernateLazyInitializer", "handler"})
    private Usuario usuario;

    @NotBlank(message = "El motivo de la disputa es obligatorio")
    @Column(nullable = false, length = 100)
    private String motivo;

    @NotBlank(message = "Debe proporcionar una descripción del problema")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Column(name = "evidencia_json", columnDefinition = "JSON")
    private String evidenciaJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDisputa estado = EstadoDisputa.abierta;

    @Column(name = "fecha_apertura", nullable = false, updatable = false, insertable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    public enum EstadoDisputa { abierta, en_revision, resuelta_reembolso, resuelta_rechazada }

    @PrePersist
    protected void onCreate() {
        this.fechaApertura = LocalDateTime.now();
    }

    public Disputa() {}

    public Integer getIdDisputa() { return idDisputa; }
    public void setIdDisputa(Integer idDisputa) { this.idDisputa = idDisputa; }

    public OrdenDetalle getOrdenDetalle() { return ordenDetalle; }
    public void setOrdenDetalle(OrdenDetalle ordenDetalle) { this.ordenDetalle = ordenDetalle; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEvidenciaJson() { return evidenciaJson; }
    public void setEvidenciaJson(String evidenciaJson) { this.evidenciaJson = evidenciaJson; }

    public EstadoDisputa getEstado() { return estado; }
    public void setEstado(EstadoDisputa estado) { this.estado = estado; }

    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }
}