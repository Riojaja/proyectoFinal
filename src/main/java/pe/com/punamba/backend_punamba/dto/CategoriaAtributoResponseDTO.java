package pe.com.punamba.backend_punamba.dto;

import java.util.List;

public class CategoriaAtributoResponseDTO {
    private Integer idAtributo;
    private String nombre;
    private String tipoDato;
    private String unidad;
    private Boolean obligatorio;
    private Integer orden;
    private List<AtributoOpcionDTO> opciones;

    public Integer getIdAtributo() {
        return idAtributo;
    }

    public void setIdAtributo(Integer idAtributo) {
        this.idAtributo = idAtributo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoDato() {
        return tipoDato;
    }

    public void setTipoDato(String tipoDato) {
        this.tipoDato = tipoDato;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public Boolean getObligatorio() {
        return obligatorio;
    }

    public void setObligatorio(Boolean obligatorio) {
        this.obligatorio = obligatorio;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public List<AtributoOpcionDTO> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<AtributoOpcionDTO> opciones) {
        this.opciones = opciones;
    }
}