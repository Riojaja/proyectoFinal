package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.*;
import pe.com.punamba.backend_punamba.repository.*;

import java.util.List;

@Service
public class PagoService {

    @Autowired private TransaccionRepository transaccionRepository;
    @Autowired private OrdenRepository ordenRepository;
    @Autowired private MetodoPagoRepository metodoPagoRepository;
    @Autowired private EstadoOrdenRepository estadoOrdenRepository;

    @Transactional(readOnly = true)
    public List<MetodoPago> listarMetodosActivos() {
        return metodoPagoRepository.findByActivoTrue();
    }

    @Transactional
    public Transaccion procesarPago(Integer idOrden, Integer idMetodo, String numOperacion) {
        Orden orden = ordenRepository.findById(idOrden)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
        
        if (orden.getEstado().getNombre().equalsIgnoreCase("PAGADO")) {
            throw new RuntimeException("Esta orden ya ha sido pagada previamente.");
        }

        MetodoPago metodo = metodoPagoRepository.findById(idMetodo)
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado"));

        EstadoOrden estadoPagado = estadoOrdenRepository.findByNombre("PAGADO")
                .orElseThrow(() -> new RuntimeException("El estado PAGADO no existe en la base de datos"));

        if (numOperacion != null && !numOperacion.isEmpty() && transaccionRepository.findByNumeroTransaccion(numOperacion).isPresent()) {
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