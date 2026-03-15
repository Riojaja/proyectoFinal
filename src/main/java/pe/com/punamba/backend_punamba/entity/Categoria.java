package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// import com.fasterxml.jackson.annotation.JsonManagedReference; // Comentado porque no se usa ahora
// import com.fasterxml.jackson.annotation.JsonBackReference;    // Comentado porque no se usa ahora
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Column(length = 100, nullable = false, unique = true)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "icono")
    private String icono;

    @Column(name = "estado")
    private Boolean estado = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_padre")
    //@JsonBackReference // Opción para mostrar relación jerárquica
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "subcategorias"})
    private Categoria categoriaPadre;

    @JsonIgnore // ESTO ES LO QUE FUNCIONÓ: Mantiene el JSON limpio y sin error 500
    //@JsonManagedReference // COMENTADO: Actívalo si necesitas ver subcategorías en el frontend
    @OneToMany(mappedBy = "categoriaPadre", cascade = CascadeType.ALL)
    private List<Categoria> subcategorias;

    @Column(name = "fecha_creacion", nullable = false, updatable = false, insertable = false)
    private LocalDateTime fechaCreacion;

    public Categoria() {}

    public Integer getIdCategoria() { 
        return idCategoria; 
    }

    public void setIdCategoria(Integer idCategoria) { 
        this.idCategoria = idCategoria; 
    }

    public String getNombre() { 
        return nombre; 
    }

    public void setNombre(String nombre) { 
        this.nombre = nombre; 
    }

    public String getDescripcion() { 
        return descripcion; 
    }

    public void setDescripcion(String descripcion) { 
        this.descripcion = descripcion; 
    }

    public String getIcono() { 
        return icono; 
    }

    public void setIcono(String icono) { 
        this.icono = icono; 
    }

    public Boolean getEstado() { 
        return estado; 
    }

    public void setEstado(Boolean estado) { 
        this.estado = estado; 
    }

    public Categoria getCategoriaPadre() { 
        return categoriaPadre; 
    }

    public void setCategoriaPadre(Categoria categoriaPadre) { 
        this.categoriaPadre = categoriaPadre; 
    }

    public List<Categoria> getSubcategorias() { 
        return subcategorias; 
    }

    public void setSubcategorias(List<Categoria> subcategorias) { 
        this.subcategorias = subcategorias; 
    }

    public LocalDateTime getFechaCreacion() { 
        return fechaCreacion; 
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) { 
        this.fechaCreacion = fechaCreacion; 
    }
}