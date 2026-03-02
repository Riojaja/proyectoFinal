package pe.com.punamba.backend_punamba.dto;

public class VarianteAtributoResponseDTO {

    private Integer idAtributo;
    private String nombreAtributo;
    
    private Integer idValor;
    private String valor;

    public VarianteAtributoResponseDTO() {}

    public Integer getIdAtributo() { return idAtributo; }
    public void setIdAtributo(Integer idAtributo) { this.idAtributo = idAtributo; }
    public String getNombreAtributo() { return nombreAtributo; }
    public void setNombreAtributo(String nombreAtributo) { this.nombreAtributo = nombreAtributo; }
    public Integer getIdValor() { return idValor; }
    public void setIdValor(Integer idValor) { this.idValor = idValor; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
}