package pe.com.punamba.backend_punamba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "ubigeos")
public class Ubigeo {

    @Id
    @NotBlank(message = "El código de ubigeo es obligatorio")
    @Column(name = "codigo_ubigeo", length = 6, nullable = false)
    private String codigoUbigeo;

    @NotBlank(message = "El departamento es obligatorio")
    @Column(nullable = false, length = 50)
    private String departamento;

    @NotBlank(message = "La provincia es obligatoria")
    @Column(nullable = false, length = 50)
    private String provincia;

    @NotBlank(message = "El distrito es obligatorio")
    @Column(nullable = false, length = 50)
    private String distrito;

    public Ubigeo() {}

    public String getCodigoUbigeo() { return codigoUbigeo; }
    public void setCodigoUbigeo(String codigoUbigeo) { this.codigoUbigeo = codigoUbigeo; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }

    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }
}