package pe.com.punamba.backend_punamba.dto;

import java.math.BigDecimal;
import java.util.List;

public class AdminDashboardDTO {

    private BigDecimal gmv;
    private BigDecimal comisiones;
    private long vendedoresPendientes;
    private long categoriasActivas;
    private List<VendedorResumenDTO> vendedores;
    private List<VentaPeriodoDTO> ventasMensuales;
    private List<VentaPeriodoDTO> ventasSemanales;

    public AdminDashboardDTO() {
    }

    public AdminDashboardDTO(
            BigDecimal gmv,
            BigDecimal comisiones,
            long vendedoresPendientes,
            long categoriasActivas,
            List<VendedorResumenDTO> vendedores,
            List<VentaPeriodoDTO> ventasMensuales,
            List<VentaPeriodoDTO> ventasSemanales
    ) {
        this.gmv = gmv;
        this.comisiones = comisiones;
        this.vendedoresPendientes = vendedoresPendientes;
        this.categoriasActivas = categoriasActivas;
        this.vendedores = vendedores;
        this.ventasMensuales = ventasMensuales;
        this.ventasSemanales = ventasSemanales;
    }

    public BigDecimal getGmv() {
        return gmv;
    }

    public void setGmv(BigDecimal gmv) {
        this.gmv = gmv;
    }

    public BigDecimal getComisiones() {
        return comisiones;
    }

    public void setComisiones(BigDecimal comisiones) {
        this.comisiones = comisiones;
    }

    public long getVendedoresPendientes() {
        return vendedoresPendientes;
    }

    public void setVendedoresPendientes(long vendedoresPendientes) {
        this.vendedoresPendientes = vendedoresPendientes;
    }

    public long getCategoriasActivas() {
        return categoriasActivas;
    }

    public void setCategoriasActivas(long categoriasActivas) {
        this.categoriasActivas = categoriasActivas;
    }

    public List<VendedorResumenDTO> getVendedores() {
        return vendedores;
    }

    public void setVendedores(List<VendedorResumenDTO> vendedores) {
        this.vendedores = vendedores;
    }

    public List<VentaPeriodoDTO> getVentasMensuales() {
        return ventasMensuales;
    }

    public void setVentasMensuales(List<VentaPeriodoDTO> ventasMensuales) {
        this.ventasMensuales = ventasMensuales;
    }

    public List<VentaPeriodoDTO> getVentasSemanales() {
        return ventasSemanales;
    }

    public void setVentasSemanales(List<VentaPeriodoDTO> ventasSemanales) {
        this.ventasSemanales = ventasSemanales;
    }

    public static class VendedorResumenDTO {
        private Integer idVendedor;
        private String nombre;
        private String logoUrl;
        private Integer totalVentas;

        public VendedorResumenDTO() {
        }

        public VendedorResumenDTO(Integer idVendedor, String nombre, String logoUrl, Integer totalVentas) {
            this.idVendedor = idVendedor;
            this.nombre = nombre;
            this.logoUrl = logoUrl;
            this.totalVentas = totalVentas;
        }

        public Integer getIdVendedor() {
            return idVendedor;
        }

        public void setIdVendedor(Integer idVendedor) {
            this.idVendedor = idVendedor;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getLogoUrl() {
            return logoUrl;
        }

        public void setLogoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
        }

        public Integer getTotalVentas() {
            return totalVentas;
        }

        public void setTotalVentas(Integer totalVentas) {
            this.totalVentas = totalVentas;
        }
    }

    public static class VentaPeriodoDTO {
        private int periodo;
        private BigDecimal total;

        public VentaPeriodoDTO() {
        }

        public VentaPeriodoDTO(int periodo, BigDecimal total) {
            this.periodo = periodo;
            this.total = total;
        }

        public int getPeriodo() {
            return periodo;
        }

        public void setPeriodo(int periodo) {
            this.periodo = periodo;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }
    }
}