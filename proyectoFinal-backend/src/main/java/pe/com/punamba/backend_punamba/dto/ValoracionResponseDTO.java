package pe.com.punamba.backend_punamba.dto;

import java.time.LocalDateTime;

public class ValoracionResponseDTO {
    private Integer idValoracion;
    private String nombreUsuario;
    private Integer calificacion;
    private String titulo;
    private String comentario;
    private String imagenes;
    private Boolean verificada;
    private LocalDateTime fechaCreacion;

    public Integer getIdValoracion() { return idValoracion; }
    public void setIdValoracion(Integer idValoracion) { this.idValoracion = idValoracion; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public Integer getCalificacion() { return calificacion; }
    public void setCalificacion(Integer calificacion) { this.calificacion = calificacion; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public String getImagenes() { return imagenes; }
    public void setImagenes(String imagenes) { this.imagenes = imagenes; }
    public Boolean getVerificada() { return verificada; }
    public void setVerificada(Boolean verificada) { this.verificada = verificada; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}