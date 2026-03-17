package pe.com.punamba.backend_punamba.dto;

public class AtributoValorRequestDTO {
    private Integer idAtributo;
    private String valor;

    public Integer getIdAtributo() {
        return idAtributo;
    }

    public void setIdAtributo(Integer idAtributo) {
        this.idAtributo = idAtributo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}