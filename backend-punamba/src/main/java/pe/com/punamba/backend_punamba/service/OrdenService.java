package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.*;
import pe.com.punamba.backend_punamba.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrdenService {

    @Autowired private OrdenRepository ordenRepository;
    @Autowired private CarritoService carritoService;
    @Autowired private EstadoOrdenRepository estadoRepository;
    @Autowired private ProductoVarianteRepository varianteRepository;
    @Autowired private DireccionRepository direccionRepository; 
    @Autowired private MetodoEnvioRepository metodoEnvioRepository;

    @Transactional
    public Orden crearOrdenDesdeCarrito(Integer idUsuario, Integer idDireccion, String notas) {
        Carrito carrito = carritoService.obtenerPorUsuario(idUsuario);
        
        if (carrito.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío. No se puede generar la orden.");
        }

        EstadoOrden estadoInicial = estadoRepository.findByNombre("PENDIENTE")
                .orElseThrow(() -> new RuntimeException("Estado PENDIENTE no configurado en la DB"));

        Direccion direccion = direccionRepository.findById(idDireccion)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));

        MetodoEnvio metodoEnvioDefault = metodoEnvioRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Método de envío por defecto no configurado"));

        Orden orden = new Orden();
        orden.setUsuario(carrito.getUsuario());
        orden.setDireccion(direccion);
        orden.setEstado(estadoInicial);
        orden.setNotas(notas);

        List<OrdenDetalle> detalles = new ArrayList<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;

        for (CarritoItem item : carrito.getItems()) {
            ProductoVariante variante = item.getVariante();
            
            if (variante.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el SKU: " + variante.getSku());
            }

            OrdenDetalle detalle = new OrdenDetalle();
            detalle.setOrden(orden);
            detalle.setVariante(variante);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitarioSnapshot(variante.getPrecio());
            detalle.setVendedor(variante.getProducto().getVendedor());
            detalle.setMetodoEnvio(metodoEnvioDefault);
            
            BigDecimal subtotalItem = variante.getPrecio().multiply(new BigDecimal(item.getCantidad()));
            detalle.setSubtotal(subtotalItem);
            
            detalle.setComisionPlataforma(subtotalItem.multiply(new BigDecimal("0.10")));

            detalles.add(detalle);
            totalGeneral = totalGeneral.add(subtotalItem);

            variante.setStock(variante.getStock() - item.getCantidad());
            varianteRepository.save(variante);
        }

        orden.setDetalles(detalles);
        
        BigDecimal divisorIgv = new BigDecimal("1.18");
        BigDecimal subtotalSinIgv = totalGeneral.divide(divisorIgv, 2, RoundingMode.HALF_UP);
        BigDecimal impuestoIgv = totalGeneral.subtract(subtotalSinIgv);

        orden.setTotal(totalGeneral);
        orden.setSubtotal(subtotalSinIgv);
        orden.setImpuesto(impuestoIgv); 
        orden.setCostoEnvio(BigDecimal.ZERO);

        ordenRepository.save(orden);
        carritoService.limpiarCarrito(idUsuario);

        return orden;
    }

    @Transactional(readOnly = true)
    public List<Orden> listarPorUsuario(Integer idUsuario) {
        return ordenRepository.findByUsuarioIdUsuarioOrderByFechaOrdenDesc(idUsuario);
    }
}