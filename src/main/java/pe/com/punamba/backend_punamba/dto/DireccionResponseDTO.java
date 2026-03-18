package pe.com.punamba.backend_punamba.dto;

public class DireccionResponseDTO {

    private Integer idDireccion;
    private String nombreDestinatario;
    private String telefono;
    private String direccionLinea1;
    private String direccionLinea2;
    private Boolean esPrincipal;
    private UbigeoDTO ubigeo;

    public static class UbigeoDTO {
        private String codigoUbigeo;
        private String departamento;
        private String provincia;
        private String distrito;

        public String getCodigoUbigeo() {
            return codigoUbigeo;
        }

        public void setCodigoUbigeo(String codigoUbigeo) {
            this.codigoUbigeo = codigoUbigeo;
        }

        public String getDepartamento() {
            return departamento;
        }

        public void setDepartamento(String departamento) {
            this.departamento = departamento;
        }

        public String getProvincia() {
            return provincia;
        }

        public void setProvincia(String provincia) {
            this.provincia = provincia;
        }

        public String getDistrito() {
            return distrito;
        }

        public void setDistrito(String distrito) {
            this.distrito = distrito;
        }
    }

    public Integer getIdDireccion() {
        return idDireccion;
    }

    public void setIdDireccion(Integer idDireccion) {
        this.idDireccion = idDireccion;
    }

    public String getNombreDestinatario() {
        return nombreDestinatario;
    }

    public void setNombreDestinatario(String nombreDestinatario) {
        this.nombreDestinatario = nombreDestinatario;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccionLinea1() {
        return direccionLinea1;
    }

    public void setDireccionLinea1(String direccionLinea1) {
        this.direccionLinea1 = direccionLinea1;
    }

    public String getDireccionLinea2() {
        return direccionLinea2;
    }

    public void setDireccionLinea2(String direccionLinea2) {
        this.direccionLinea2 = direccionLinea2;
    }

    public Boolean getEsPrincipal() {
        return esPrincipal;
    }

    public void setEsPrincipal(Boolean esPrincipal) {
        this.esPrincipal = esPrincipal;
    }

    public UbigeoDTO getUbigeo() {
        return ubigeo;
    }

    public void setUbigeo(UbigeoDTO ubigeo) {
        this.ubigeo = ubigeo;
    }
}