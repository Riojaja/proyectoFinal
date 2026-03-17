package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "variantes_producto")
public class ProductoVariante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_variante")
    private Integer idVariante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    @JsonBackReference(value = "producto-variante")
    private Producto producto;

    @NotBlank(message = "El SKU es obligatorio")
    @Column(unique = true, nullable = false, length = 100)
    private String sku;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "precio_oferta", precision = 10, scale = 2)
    private BigDecimal precioOferta;

    @NotNull(message = "El stock es obligatorio")
    @Min(0)
    @Column(nullable = false)
    private Integer stock;

    @Column(name = "stock_reservado", nullable = false)
    private Integer stockReservado = 0;

    private Boolean activo = true;

    @OneToMany(mappedBy = "variante", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "variante-atributos")
    private List<VarianteAtributo> atributos = new ArrayList<>();

    @Column(name = "fecha_creacion", nullable = false, updatable = false, insertable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false, updatable = false, insertable = false)
    private LocalDateTime fechaActualizacion;

    public ProductoVariante() {}

    public Integer getIdVariante() { return idVariante; }
    public void setIdVariante(Integer idVariante) { this.idVariante = idVariante; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public BigDecimal getPrecioOferta() { return precioOferta; }
    public void setPrecioOferta(BigDecimal precioOferta) { this.precioOferta = precioOferta; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getStockReservado() { return stockReservado; }
    public void setStockReservado(Integer stockReservado) { this.stockReservado = stockReservado; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public List<VarianteAtributo> getAtributos() { return atributos; }
    public void setAtributos(List<VarianteAtributo> atributos) { 
        this.atributos = atributos; 
        if (atributos != null) {
            for (VarianteAtributo attr : atributos) {
                attr.setVariante(this);
            }
        }
    }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}