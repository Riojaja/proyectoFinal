package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Cupon;
import pe.com.punamba.backend_punamba.repository.CuponRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CuponService {

    @Autowired private CuponRepository cuponRepository;

    @Transactional(readOnly = true)
    public List<Cupon> listarTodos() {
        return cuponRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Cupon> buscarPorId(Integer id) {
        return cuponRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Cupon> listarPorVendedor(Integer idVendedor) {
        return cuponRepository.findByVendedorIdVendedor(idVendedor);
    }

    @Transactional(readOnly = true)
    public Cupon validarCupon(String codigo, BigDecimal subtotalCarrito) {
        Cupon cupon = cuponRepository.findByCodigoAndActivoTrue(codigo)
                .orElseThrow(() -> new RuntimeException("Cupón no válido o inactivo"));

        LocalDateTime ahora = LocalDateTime.now();

        if (ahora.isBefore(cupon.getFechaInicio()) || ahora.isAfter(cupon.getFechaExpiracion())) {
            throw new RuntimeException("El cupón ha expirado o aún no es válido.");
        }

        if (cupon.getCantidadMaximaUsos() != null && cupon.getCantidadUsos() >= cupon.getCantidadMaximaUsos()) {
            throw new RuntimeException("Este cupón ha alcanzado su límite de usos.");
        }

        if (cupon.getMontoMinimo() != null && subtotalCarrito.compareTo(cupon.getMontoMinimo()) < 0) {
            throw new RuntimeException("El subtotal no alcanza el monto mínimo para este cupón.");
        }

        return cupon;
    }

    @Transactional
    public void registrarUsoCupon(Integer idCupon) {
        Cupon cupon = cuponRepository.findById(idCupon).orElseThrow();
        cupon.setCantidadUsos(cupon.getCantidadUsos() + 1);
        
        if (cupon.getCantidadMaximaUsos() != null && cupon.getCantidadUsos() >= cupon.getCantidadMaximaUsos()) {
            cupon.setActivo(false);
        }
        
        cuponRepository.save(cupon);
    }

    @Transactional
    public Cupon crearCupon(Cupon cupon) {
        if (cupon.getFechaExpiracion().isBefore(cupon.getFechaInicio())) {
            throw new RuntimeException("La fecha de expiración no puede ser menor a la de inicio.");
        }
        cupon.setCodigo(cupon.getCodigo().toUpperCase());
        return cuponRepository.save(cupon);
    }

    @Transactional
    public Cupon actualizar(Cupon cupon) {
        if (cupon.getFechaExpiracion().isBefore(cupon.getFechaInicio())) {
            throw new RuntimeException("La fecha de expiración no puede ser menor a la de inicio.");
        }
        cupon.setCodigo(cupon.getCodigo().toUpperCase());
        return cuponRepository.save(cupon);
    }

    @Transactional
    public void eliminar(Integer id) {
        cuponRepository.findById(id).ifPresent(c -> {
            c.setActivo(false);
            cuponRepository.save(c);
        });
    }

    public boolean existeCodigo(String codigo) {
        return cuponRepository.existsByCodigo(codigo);
    }
}