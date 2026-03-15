package pe.com.punamba.backend_punamba.dto;

import jakarta.validation.constraints.NotNull;

public class VarianteAtributoRequestDTO {

    @NotNull(message = "El ID del atributo es obligatorio")
    private Integer idAtributo;

    @NotNull(message = "El ID del valor es obligatorio")
    private Integer idValor;

    public VarianteAtributoRequestDTO() {}

    public Integer getIdAtributo() { return idAtributo; }
    public void setIdAtributo(Integer idAtributo) { this.idAtributo = idAtributo; }
    public Integer getIdValor() { return idValor; }
    public void setIdValor(Integer idValor) { this.idValor = idValor; }
}