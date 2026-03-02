package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonBackReference; // Cambio clave
import java.math.BigDecimal;

@Entity
@Table(name = "carrito_items")
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Integer idItem;

    @ManyToOne
    @JoinColumn(name = "id_carrito", nullable = false)
    @JsonBackReference(value = "carrito-item")
    private Carrito carrito;

    @ManyToOne
    @JoinColumn(name = "id_variante", nullable = false)
    private ProductoVariante variante;

    @Column(nullable = false)
    @Min(1)
    private Integer cantidad;

    @Column(name = "precio_snapshot", nullable = false)
    private BigDecimal precioSnapshot;

    public CarritoItem() {}

    public Integer getIdItem() { return idItem; }
    public void setIdItem(Integer idItem) { this.idItem = idItem; }
    public Carrito getCarrito() { return carrito; }
    public void setCarrito(Carrito carrito) { this.carrito = carrito; }
    public ProductoVariante getVariante() { return variante; }
    public void setVariante(ProductoVariante variante) { this.variante = variante; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioSnapshot() { return precioSnapshot; }
    public void setPrecioSnapshot(BigDecimal precioSnapshot) { this.precioSnapshot = precioSnapshot; }
}