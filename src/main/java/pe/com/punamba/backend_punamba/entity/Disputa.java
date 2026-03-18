package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
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

    @Column(name = "monto_reembolso", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoReembolso = BigDecimal.ZERO;

    @Column(name = "observacion_admin", columnDefinition = "TEXT")
    private String observacionAdmin;

    @Column(name = "resolucion_final", columnDefinition = "TEXT")
    private String resolucionFinal;

    @Column(name = "fecha_apertura", nullable = false, updatable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    public enum EstadoDisputa {
        abierta,
        en_revision,
        en_negociacion,
        resuelta_reembolso_total,
        resuelta_reembolso_parcial,
        resuelta_rechazada,
        cerrada
    }

    @PrePersist
    protected void onCreate() {
        if (this.fechaApertura == null) {
            this.fechaApertura = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoDisputa.abierta;
        }
        if (this.montoReembolso == null) {
            this.montoReembolso = BigDecimal.ZERO;
        }
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

    public BigDecimal getMontoReembolso() { return montoReembolso; }
    public void setMontoReembolso(BigDecimal montoReembolso) { this.montoReembolso = montoReembolso; }

    public String getObservacionAdmin() { return observacionAdmin; }
    public void setObservacionAdmin(String observacionAdmin) { this.observacionAdmin = observacionAdmin; }

    public String getResolucionFinal() { return resolucionFinal; }
    public void setResolucionFinal(String resolucionFinal) { this.resolucionFinal = resolucionFinal; }

    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
}