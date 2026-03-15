package pe.com.punamba.backend_punamba.dto;

public class MarcaResponseDTO {
    private Integer idMarca;
    private String nombre;
    private Boolean estado;

    public MarcaResponseDTO() {
    }

    public MarcaResponseDTO(Integer idMarca, String nombre, Boolean estado) {
        this.idMarca = idMarca;
        this.nombre = nombre;
        this.estado = estado;
    }

    public Integer getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(Integer idMarca) {
        this.idMarca = idMarca;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}