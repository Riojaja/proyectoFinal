package pe.com.punamba.backend_punamba.dto;

public class CategoriaPromoDTO {

    private Integer idCategoria;
    private String nombre;
    private String imagen;
    private String texto;
    private String precioReferencia;

    public CategoriaPromoDTO() {
    }

    public CategoriaPromoDTO(Integer idCategoria, String nombre, String imagen, String texto, String precioReferencia) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.imagen = imagen;
        this.texto = texto;
        this.precioReferencia = precioReferencia;
    }

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

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getPrecioReferencia() {
        return precioReferencia;
    }

    public void setPrecioReferencia(String precioReferencia) {
        this.precioReferencia = precioReferencia;
    }
}
