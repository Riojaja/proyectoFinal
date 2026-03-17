package pe.com.punamba.backend_punamba.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ValoracionRequestDTO {
    
    @NotNull(message = "El ID de la variante es obligatorio")
    private Integer idVariante;
    
    private Integer idOrdenDetalle;

    @Min(value = 1, message = "La calificación mínima es 1 estrella") 
    @Max(value = 5, message = "La calificación máxima es 5 estrellas")
    @NotNull(message = "La calificación es obligatoria")
    private Integer calificacion;

    private String titulo;
    private String comentario;
    private String imagenes;

    public Integer getIdVariante() { return idVariante; }
    public void setIdVariante(Integer idVariante) { this.idVariante = idVariante; }
    public Integer getIdOrdenDetalle() { return idOrdenDetalle; }
    public void setIdOrdenDetalle(Integer idOrdenDetalle) { this.idOrdenDetalle = idOrdenDetalle; }
    public Integer getCalificacion() { return calificacion; }
    public void setCalificacion(Integer calificacion) { this.calificacion = calificacion; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public String getImagenes() { return imagenes; }
    public void setImagenes(String imagenes) { this.imagenes = imagenes; }
}