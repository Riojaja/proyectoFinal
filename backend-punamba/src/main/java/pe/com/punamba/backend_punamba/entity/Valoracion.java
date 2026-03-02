package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "valoraciones")
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_valoracion")
    private Integer idValoracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_variante", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Evita errores de carga perezosa
    private ProductoVariante variante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "roles", "hibernateLazyInitializer", "handler"}) // Protege los datos sensibles del usuario
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden_detalle")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private OrdenDetalle ordenDetalle;

    @Min(value = 1, message = "La calificación mínima es 1 estrella") 
    @Max(value = 5, message = "La calificación máxima es 5 estrellas")
    @NotNull(message = "La calificación es obligatoria")
    @Column(nullable = false)
    private Integer calificacion;

    @Column(length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(columnDefinition = "JSON")
    private String imagenes;

    private Boolean verificada = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false, insertable = false)
    private LocalDateTime fechaCreacion;

    public Valoracion() {}

    public Integer getIdValoracion() { return idValoracion; }
    public void setIdValoracion(Integer idValoracion) { this.idValoracion = idValoracion; }

    public ProductoVariante getVariante() { return variante; }
    public void setVariante(ProductoVariante variante) { this.variante = variante; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public OrdenDetalle getOrdenDetalle() { return ordenDetalle; }
    public void setOrdenDetalle(OrdenDetalle ordenDetalle) { this.ordenDetalle = ordenDetalle; }

    public Integer getCalificacion() { return calificacion; }
    public void setCalificacion(Integer calificacion) { this.calificacion = calificacion; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getImagenes() { return imagenes; }
    public void setImagenes(String imagenes) { this.imagenes = imagenes; }

    public Boolean getVerificada() { return verificada; }
    public void setVerificada(Boolean verificada) { this.verificada = verificada; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}