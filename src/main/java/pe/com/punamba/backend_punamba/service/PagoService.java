package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.EstadoOrden;
import pe.com.punamba.backend_punamba.entity.MetodoPago;
import pe.com.punamba.backend_punamba.entity.Orden;
import pe.com.punamba.backend_punamba.entity.Transaccion;
import pe.com.punamba.backend_punamba.repository.EstadoOrdenRepository;
import pe.com.punamba.backend_punamba.repository.MetodoPagoRepository;
import pe.com.punamba.backend_punamba.repository.OrdenRepository;
import pe.com.punamba.backend_punamba.repository.TransaccionRepository;

import java.util.List;
import java.util.Objects;

@Service
public class PagoService {

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private EstadoOrdenRepository estadoOrdenRepository;

    @Transactional(readOnly = true)
    public List<MetodoPago> listarMetodosActivos() {
        return metodoPagoRepository.findByActivoTrue();
    }

    @Transactional
    public Transaccion procesarPago(Integer idOrden, Integer idMetodo, String numOperacion) {
        Integer idOrdenSeguro = Objects.requireNonNull(idOrden, "El id de la orden no puede ser null");
        Integer idMetodoSeguro = Objects.requireNonNull(idMetodo, "El id del método de pago no puede ser null");

        Orden orden = ordenRepository.findById(idOrdenSeguro)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        if (orden.getEstado().getNombre().equalsIgnoreCase("PAGADO")) {
            throw new RuntimeException("Esta orden ya ha sido pagada previamente.");
        }

        MetodoPago metodo = metodoPagoRepository.findById(idMetodoSeguro)
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado"));

        EstadoOrden estadoPagado = estadoOrdenRepository.findByNombre("PAGADO")
                .orElseThrow(() -> new RuntimeException("El estado PAGADO no existe en la base de datos"));

        if (numOperacion != null
                && !numOperacion.isEmpty()
                && transaccionRepository.findByNumeroTransaccion(numOperacion).isPresent()) {
            throw new RuntimeException("Este número de operación ya ha sido registrado.");
        }

        Transaccion trx = new Transaccion();
        trx.setOrden(orden);
        trx.setMetodoPago(metodo);
        trx.setMonto(orden.getTotal());
        trx.setNumeroTransaccion(numOperacion);
        trx.setEstado(Transaccion.EstadoTransaccion.completado);

        orden.setEstado(estadoPagado);
        ordenRepository.save(orden);

        return transaccionRepository.save(trx);
    }
}