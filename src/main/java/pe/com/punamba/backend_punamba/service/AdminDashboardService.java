package pe.com.punamba.backend_punamba.service;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import pe.com.punamba.backend_punamba.dto.AdminDashboardDTO;
import pe.com.punamba.backend_punamba.entity.Vendedor;
import pe.com.punamba.backend_punamba.repository.CategoriaRepository;
import pe.com.punamba.backend_punamba.repository.OrdenDetalleRepository;
import pe.com.punamba.backend_punamba.repository.OrdenRepository;
import pe.com.punamba.backend_punamba.repository.VendedorRepository;

@Service
public class AdminDashboardService {

    private final OrdenRepository ordenRepository;
    private final OrdenDetalleRepository ordenDetalleRepository;
    private final VendedorRepository vendedorRepository;
    private final CategoriaRepository categoriaRepository;

    public AdminDashboardService(
            OrdenRepository ordenRepository,
            OrdenDetalleRepository ordenDetalleRepository,
            VendedorRepository vendedorRepository,
            CategoriaRepository categoriaRepository) {
        this.ordenRepository = ordenRepository;
        this.ordenDetalleRepository = ordenDetalleRepository;
        this.vendedorRepository = vendedorRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public AdminDashboardDTO obtenerDashboard() {
        BigDecimal gmv = safe(ordenRepository.obtenerGmvTotal());
        BigDecimal comisiones = safe(ordenDetalleRepository.obtenerComisionesTotales());

        long vendedoresPendientes = vendedorRepository.countByEstado(Vendedor.EstadoVendedor.pendiente);
        long categoriasActivas = categoriaRepository.countByEstadoTrue();

        List<AdminDashboardDTO.VendedorResumenDTO> vendedores = vendedorRepository.findTop5ByOrderByFechaRegistroDesc()
                .stream()
                .map(v -> {
                    Integer totalVentas = v.getTotalVentas();
                    return new AdminDashboardDTO.VendedorResumenDTO(
                            v.getIdVendedor(),
                            v.getNombreTienda(),
                            v.getLogoUrl(),
                            totalVentas == null ? 0 : totalVentas);
                })
                .toList();

        int anioActual = Year.now().getValue();

        List<AdminDashboardDTO.VentaPeriodoDTO> ventasMensuales = construirVentasMensuales(anioActual);
        List<AdminDashboardDTO.VentaPeriodoDTO> ventasSemanales = construirVentasSemanales(anioActual);

        return new AdminDashboardDTO(
                gmv,
                comisiones,
                vendedoresPendientes,
                categoriasActivas,
                vendedores,
                ventasMensuales,
                ventasSemanales);
    }

    private List<AdminDashboardDTO.VentaPeriodoDTO> construirVentasMensuales(int anio) {
        List<Object[]> rawVentas = ordenRepository.obtenerVentasMensuales(anio);
        Map<Integer, BigDecimal> ventasPorMes = new HashMap<>();

        for (Object[] row : rawVentas) {
            if (row == null || row.length < 2 || row[0] == null) {
                continue;
            }

            int mes = ((Number) row[0]).intValue();
            BigDecimal total = toBigDecimal(row[1]);
            ventasPorMes.put(mes, total);
        }

        List<AdminDashboardDTO.VentaPeriodoDTO> ventasMensuales = new ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            ventasMensuales.add(new AdminDashboardDTO.VentaPeriodoDTO(
                    mes,
                    ventasPorMes.getOrDefault(mes, BigDecimal.ZERO)));
        }

        return ventasMensuales;
    }

    private List<AdminDashboardDTO.VentaPeriodoDTO> construirVentasSemanales(int anio) {
        List<Object[]> rawVentas = ordenRepository.obtenerVentasSemanales(anio);
        Map<Integer, BigDecimal> ventasPorSemana = new HashMap<>();

        for (Object[] row : rawVentas) {
            if (row == null || row.length < 2 || row[0] == null) {
                continue;
            }

            int semana = ((Number) row[0]).intValue();
            BigDecimal total = toBigDecimal(row[1]);
            ventasPorSemana.put(semana, total);
        }

        List<AdminDashboardDTO.VentaPeriodoDTO> ventasSemanales = new ArrayList<>();

        for (int semana = 1; semana <= 53; semana++) {
            ventasSemanales.add(new AdminDashboardDTO.VentaPeriodoDTO(
                    semana,
                    ventasPorSemana.getOrDefault(semana, BigDecimal.ZERO)));
        }

        return ventasSemanales;
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        return new BigDecimal(value.toString());
    }
}