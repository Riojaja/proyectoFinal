package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "favoritos")
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idFavorito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "roles", "hibernateLazyInitializer", "handler"})
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Producto producto;

    @Column(name = "fecha_agregado", nullable = false, updatable = false, insertable = false)
    private LocalDateTime fechaAgregado;

    @PrePersist
    protected void onCreate() {
        this.fechaAgregado = LocalDateTime.now();
    }

    public Favorito() {}

    public Integer getIdFavorito() { return idFavorito; }
    public void setIdFavorito(Integer idFavorito) { this.idFavorito = idFavorito; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public LocalDateTime getFechaAgregado() { return fechaAgregado; }
    public void setFechaAgregado(LocalDateTime fechaAgregado) { this.fechaAgregado = fechaAgregado; }
}