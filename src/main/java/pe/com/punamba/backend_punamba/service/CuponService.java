package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Cupon;
import pe.com.punamba.backend_punamba.repository.CuponRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CuponService {

    @Autowired
    private CuponRepository cuponRepository;

    @Transactional(readOnly = true)
    public List<Cupon> listarTodos() {
        return cuponRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Cupon> buscarPorId(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id del cupón no puede ser null");
        return cuponRepository.findById(idSeguro);
    }

    @Transactional(readOnly = true)
    public List<Cupon> listarPorVendedor(Integer idVendedor) {
        Integer idVendedorSeguro = Objects.requireNonNull(idVendedor, "El id del vendedor no puede ser null");
        return cuponRepository.findByVendedorIdVendedor(idVendedorSeguro);
    }

    @Transactional(readOnly = true)
    public boolean existeCodigo(String codigo) {
        return cuponRepository.existsByCodigoIgnoreCase(codigo);
    }

    @Transactional(readOnly = true)
    public Cupon validarCupon(String codigo, BigDecimal subtotalCarrito) {
        Cupon cupon = cuponRepository.findByCodigoAndActivoTrue(codigo.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Cupón no válido o inactivo"));

        LocalDateTime ahora = LocalDateTime.now();

        if (cupon.getFechaInicio() == null || cupon.getFechaExpiracion() == null) {
            throw new RuntimeException("El cupón no tiene fechas válidas configuradas.");
        }

        if (ahora.isBefore(cupon.getFechaInicio())) {
            throw new RuntimeException("El cupón aún no está vigente.");
        }

        if (ahora.isAfter(cupon.getFechaExpiracion())) {
            throw new RuntimeException("El cupón ha expirado.");
        }

        Integer cantidadMaximaUsos = cupon.getCantidadMaximaUsos();
        Integer cantidadUsos = cupon.getCantidadUsos();

        if (cantidadMaximaUsos != null
                && cantidadMaximaUsos > 0
                && cantidadUsos != null
                && cantidadUsos >= cantidadMaximaUsos) {
            throw new RuntimeException("Este cupón ha alcanzado su límite de usos.");
        }

        if (cupon.getMontoMinimo() != null && subtotalCarrito.compareTo(cupon.getMontoMinimo()) < 0) {
            throw new RuntimeException("El subtotal no alcanza el monto mínimo para este cupón.");
        }

        return cupon;
    }

    @Transactional
    public void registrarUsoCupon(Integer idCupon) {
        Integer idCuponSeguro = Objects.requireNonNull(idCupon, "El id del cupón no puede ser null");

        Cupon cupon = cuponRepository.findById(idCuponSeguro)
                .orElseThrow(() -> new RuntimeException("Cupón no encontrado"));

        Integer usosActuales = cupon.getCantidadUsos();
        cupon.setCantidadUsos(usosActuales == null ? 1 : usosActuales + 1);

        Integer cantidadMaximaUsos = cupon.getCantidadMaximaUsos();
        Integer cantidadUsosActualizada = cupon.getCantidadUsos();

        if (cantidadMaximaUsos != null
                && cantidadMaximaUsos > 0
                && cantidadUsosActualizada != null
                && cantidadUsosActualizada >= cantidadMaximaUsos) {
            cupon.setActivo(false);
        }

        cuponRepository.save(cupon);
    }

    @Transactional
    public Cupon crearCupon(Cupon cupon) {
        validarCuponInterno(cupon);
        cupon.setCodigo(cupon.getCodigo().trim().toUpperCase());
        return cuponRepository.save(cupon);
    }

    @Transactional
    public Cupon actualizar(Cupon cupon) {
        validarCuponInterno(cupon);
        cupon.setCodigo(cupon.getCodigo().trim().toUpperCase());
        return cuponRepository.save(cupon);
    }

    @Transactional
    public void eliminar(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id del cupón no puede ser null");

        cuponRepository.findById(idSeguro).ifPresent(c -> {
            c.setActivo(false);
            cuponRepository.save(c);
        });
    }

    private void validarCuponInterno(Cupon cupon) {
        if (cupon.getCodigo() == null || cupon.getCodigo().trim().isEmpty()) {
            throw new RuntimeException("El código del cupón es obligatorio.");
        }

        if (cupon.getValorDescuento() == null || cupon.getValorDescuento().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El monto de descuento debe ser mayor que cero.");
        }

        if (cupon.getMontoMinimo() != null && cupon.getMontoMinimo().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El monto mínimo no puede ser negativo.");
        }

        if (cupon.getCantidadMaximaUsos() != null && cupon.getCantidadMaximaUsos() < 0) {
            throw new RuntimeException("El límite de usos no puede ser negativo.");
        }

        if (cupon.getFechaInicio() == null || cupon.getFechaExpiracion() == null) {
            throw new RuntimeException("Las fechas del cupón son obligatorias.");
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaInicio = cupon.getFechaInicio().toLocalDate();
        LocalDate fechaFin = cupon.getFechaExpiracion().toLocalDate();

        if (fechaInicio.isBefore(hoy)) {
            throw new RuntimeException("La fecha de inicio no puede ser menor que hoy.");
        }

        if (!fechaFin.isAfter(fechaInicio)) {
            throw new RuntimeException("La fecha fin debe ser mayor que la fecha inicio.");
        }

        if (cupon.getTipoDescuento() == Cupon.TipoDescuento.porcentaje
                && cupon.getValorDescuento().compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("El descuento porcentual no puede ser mayor a 100.");
        }
    }
}