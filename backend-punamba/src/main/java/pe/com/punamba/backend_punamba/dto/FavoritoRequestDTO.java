package pe.com.punamba.backend_punamba.dto;

public class FavoritoRequestDTO {
    private Integer idUsuario;
    private Integer idProducto;

    public FavoritoRequestDTO() {}

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }
}