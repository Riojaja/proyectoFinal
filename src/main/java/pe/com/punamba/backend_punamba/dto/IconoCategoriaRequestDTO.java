package pe.com.punamba.backend_punamba.dto;

public class IconoCategoriaRequestDTO {

    private String nombre;
    private String claseCss;
    private Boolean estado = true;

    public IconoCategoriaRequestDTO() {
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
}