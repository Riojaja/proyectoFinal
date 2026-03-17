package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Carrito;
import pe.com.punamba.backend_punamba.entity.CarritoItem;
import pe.com.punamba.backend_punamba.entity.Direccion;
import pe.com.punamba.backend_punamba.entity.EstadoOrden;
import pe.com.punamba.backend_punamba.entity.MetodoEnvio;
import pe.com.punamba.backend_punamba.entity.Orden;
import pe.com.punamba.backend_punamba.entity.OrdenDetalle;
import pe.com.punamba.backend_punamba.entity.ProductoVariante;
import pe.com.punamba.backend_punamba.repository.DireccionRepository;
import pe.com.punamba.backend_punamba.repository.EstadoOrdenRepository;
import pe.com.punamba.backend_punamba.repository.MetodoEnvioRepository;
import pe.com.punamba.backend_punamba.repository.OrdenRepository;
import pe.com.punamba.backend_punamba.repository.ProductoVarianteRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrdenService {

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private EstadoOrdenRepository estadoRepository;

    @Autowired
    private ProductoVarianteRepository varianteRepository;

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private MetodoEnvioRepository metodoEnvioRepository;

    @Transactional
    public Orden crearOrdenDesdeCarrito(Integer idUsuario, Integer idDireccion, String notas) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");
        Integer idDireccionSeguro = Objects.requireNonNull(idDireccion, "El id de la dirección no puede ser null");

        Carrito carrito = carritoService.obtenerPorUsuario(idUsuarioSeguro);

        if (carrito.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío. No se puede generar la orden.");
        }

        EstadoOrden estadoInicial = estadoRepository.findByNombre("PENDIENTE")
                .orElseThrow(() -> new RuntimeException("Estado PENDIENTE no configurado en la DB"));

        Direccion direccion = direccionRepository.findById(idDireccionSeguro)
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

            BigDecimal subtotalItem = variante.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
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

        Orden ordenGuardada = ordenRepository.save(Objects.requireNonNull(orden, "La orden no puede ser null"));
        carritoService.limpiarCarrito(idUsuarioSeguro);

        return ordenGuardada;
    }

    @Transactional(readOnly = true)
    public List<Orden> listarPorUsuario(Integer idUsuario) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");
        return ordenRepository.findByUsuarioIdUsuarioOrderByFechaOrdenDesc(idUsuarioSeguro);
    }
}