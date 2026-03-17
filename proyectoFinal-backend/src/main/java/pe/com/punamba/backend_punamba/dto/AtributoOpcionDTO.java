package pe.com.punamba.backend_punamba.dto;

public class AtributoOpcionDTO {
    private Integer idValor;
    private String valor;

    public AtributoOpcionDTO() {
    }

    public AtributoOpcionDTO(Integer idValor, String valor) {
        this.idValor = idValor;
        this.valor = valor;
    }

    public Integer getIdValor() {
        return idValor;
    }

    public void setIdValor(Integer idValor) {
        this.idValor = idValor;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}