package pe.com.punamba.backend_punamba.dto;

import pe.com.punamba.backend_punamba.entity.MetodoPago.TipoMetodo;

public class MetodoPagoDTO {
    private Integer idMetodoPago;
    private String nombre;
    private String descripcion;
    private TipoMetodo tipo;
    private String icono;

    public Integer getIdMetodoPago() { return idMetodoPago; }
    public void setIdMetodoPago(Integer idMetodoPago) { this.idMetodoPago = idMetodoPago; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public TipoMetodo getTipo() { return tipo; }
    public void setTipo(TipoMetodo tipo) { this.tipo = tipo; }
    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }
}