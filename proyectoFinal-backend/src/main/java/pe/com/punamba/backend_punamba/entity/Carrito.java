package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference; // Cambio clave para consistencia
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carritos")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrito")
    private Integer idCarrito;

    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "carrito-item")
    private List<CarritoItem> items = new ArrayList<>();

    public Carrito() {}

    public Integer getIdCarrito() { return idCarrito; }
    public void setIdCarrito(Integer idCarrito) { this.idCarrito = idCarrito; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    public List<CarritoItem> getItems() { return items; }
    public void setItems(List<CarritoItem> items) { 
        this.items = items; 
        if(items != null) {
            for(CarritoItem item : items) {
                item.setCarrito(this);
            }
        }
    }
}