package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "iconos_categoria")
public class IconoCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_icono")
    private Integer idIcono;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "clase_css", nullable = false, unique = true, length = 100)
    private String claseCss;

    @Column(nullable = false)
    private Boolean estado = true;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public IconoCategoria() {
    }

    public Integer getIdIcono() {
        return idIcono;
    }

    public void setIdIcono(Integer idIcono) {
        this.idIcono = idIcono;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getClaseCss() {
        return claseCss;
    }

    public void setClaseCss(String claseCss) {
        this.claseCss = claseCss;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}