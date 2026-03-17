package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos_imagenes")
public class ProductoImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Integer idImagen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    @JsonBackReference(value = "producto-imagen")
    private Producto producto;

    @Column(name = "url_imagen", nullable = false)
    private String urlImagen;

    @Column(name = "es_principal")
    private Boolean esPrincipal = false;

    private Integer orden = 0;

    @Column(name = "fecha_subida", nullable = false, updatable = false, insertable = false)
    private LocalDateTime fechaSubida;

    @PrePersist
    protected void onCreate() {
        this.fechaSubida = LocalDateTime.now();
    }

    public ProductoImagen() {}

    public Integer getIdImagen() { return idImagen; }
    public void setIdImagen(Integer idImagen) { this.idImagen = idImagen; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public String getUrlImagen() { return urlImagen; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }

    public Boolean getEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(Boolean esPrincipal) { this.esPrincipal = esPrincipal; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }
}