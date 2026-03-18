package pe.com.punamba.backend_punamba.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.punamba.backend_punamba.dto.CheckoutItemRequestDTO;
import pe.com.punamba.backend_punamba.dto.OrdenResponseDTO;
import pe.com.punamba.backend_punamba.entity.Carrito;
import pe.com.punamba.backend_punamba.entity.CarritoItem;
import pe.com.punamba.backend_punamba.entity.Direccion;
import pe.com.punamba.backend_punamba.entity.EstadoOrden;
import pe.com.punamba.backend_punamba.entity.MetodoEnvio;
import pe.com.punamba.backend_punamba.entity.Orden;
import pe.com.punamba.backend_punamba.entity.OrdenDetalle;
import pe.com.punamba.backend_punamba.entity.ProductoVariante;
import pe.com.punamba.backend_punamba.entity.Transaccion;
import pe.com.punamba.backend_punamba.mapper.OrdenMapper;
import pe.com.punamba.backend_punamba.repository.CarritoRepository;
import pe.com.punamba.backend_punamba.repository.DireccionRepository;
import pe.com.punamba.backend_punamba.repository.EstadoOrdenRepository;
import pe.com.punamba.backend_punamba.repository.MetodoEnvioRepository;
import pe.com.punamba.backend_punamba.repository.OrdenRepository;
import pe.com.punamba.backend_punamba.repository.ProductoVarianteRepository;
import pe.com.punamba.backend_punamba.repository.TransaccionRepository;

@Service
public class OrdenService {

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private EstadoOrdenRepository estadoRepository;

    @Autowired
    private ProductoVarianteRepository varianteRepository;

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private MetodoEnvioRepository metodoEnvioRepository;

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private OrdenMapper ordenMapper;

    @Transactional
    public Orden crearOrdenDesdeCarrito(Integer idUsuario, Integer idDireccion, String notas) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");
        Integer idDireccionSeguro = Objects.requireNonNull(idDireccion, "El id de la dirección no puede ser null");

        Carrito carrito = carritoService.obtenerPorUsuario(idUsuarioSeguro);

        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
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

            Integer stockDisponible = variante.getStock() != null ? variante.getStock() : 0;
            if (stockDisponible < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el SKU: " + variante.getSku());
            }

            BigDecimal precioFinal = variante.getPrecioOferta() != null
                    ? variante.getPrecioOferta()
                    : variante.getPrecio();

            OrdenDetalle detalle = new OrdenDetalle();
            detalle.setOrden(orden);
            detalle.setVariante(variante);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitarioSnapshot(precioFinal);
            detalle.setVendedor(variante.getProducto().getVendedor());
            detalle.setMetodoEnvio(metodoEnvioDefault);

            BigDecimal subtotalItem = precioFinal.multiply(BigDecimal.valueOf(item.getCantidad()));
            detalle.setSubtotal(subtotalItem);
            detalle.setComisionPlataforma(subtotalItem.multiply(new BigDecimal("0.10")));

            detalles.add(detalle);
            totalGeneral = totalGeneral.add(subtotalItem);

            variante.setStock(stockDisponible - item.getCantidad());
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

        Orden ordenGuardada = ordenRepository.save(
                Objects.requireNonNull(orden, "La orden no puede ser null"));

        carritoService.limpiarCarrito(idUsuarioSeguro);

        return ordenGuardada;
    }

    @Transactional
    public Orden crearDesdeCheckout(
            Integer idUsuario,
            Integer idDireccion,
            String notas,
            List<CheckoutItemRequestDTO> itemsSeleccionados) {

        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");
        Integer idDireccionSeguro = Objects.requireNonNull(idDireccion, "El id de la dirección no puede ser null");

        if (itemsSeleccionados == null || itemsSeleccionados.isEmpty()) {
            throw new RuntimeException("No se enviaron productos para el checkout");
        }

        Carrito carrito = carritoService.obtenerPorUsuario(idUsuarioSeguro);

        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
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

        for (CheckoutItemRequestDTO req : itemsSeleccionados) {
            if (req.getIdVariante() == null || req.getCantidad() == null || req.getCantidad() <= 0) {
                throw new RuntimeException("Item inválido en checkout");
            }

            CarritoItem itemCarrito = carrito.getItems().stream()
                    .filter(i -> i.getVariante().getIdVariante().equals(req.getIdVariante()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "La variante " + req.getIdVariante() + " no existe en el carrito"));

            if (!itemCarrito.getCantidad().equals(req.getCantidad())) {
                throw new RuntimeException(
                        "La cantidad enviada no coincide con el carrito para la variante " + req.getIdVariante());
            }

            ProductoVariante variante = itemCarrito.getVariante();

            Integer stockDisponible = variante.getStock() != null ? variante.getStock() : 0;
            if (req.getCantidad() > stockDisponible) {
                throw new RuntimeException("Stock insuficiente para el SKU: " + variante.getSku());
            }

            BigDecimal precioFinal = variante.getPrecioOferta() != null
                    ? variante.getPrecioOferta()
                    : variante.getPrecio();

            OrdenDetalle detalle = new OrdenDetalle();
            detalle.setOrden(orden);
            detalle.setVariante(variante);
            detalle.setCantidad(req.getCantidad());
            detalle.setPrecioUnitarioSnapshot(precioFinal);
            detalle.setVendedor(variante.getProducto().getVendedor());
            detalle.setMetodoEnvio(metodoEnvioDefault);

            BigDecimal subtotalItem = precioFinal.multiply(BigDecimal.valueOf(req.getCantidad()));
            detalle.setSubtotal(subtotalItem);
            detalle.setComisionPlataforma(subtotalItem.multiply(new BigDecimal("0.10")));

            detalles.add(detalle);
            totalGeneral = totalGeneral.add(subtotalItem);

            variante.setStock(stockDisponible - req.getCantidad());
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

        Orden ordenGuardada = ordenRepository.save(orden);

        carrito.getItems().removeIf(item -> itemsSeleccionados.stream()
                .anyMatch(req -> req.getIdVariante().equals(item.getVariante().getIdVariante())));

        carritoRepository.save(carrito);

        return ordenGuardada;
    }

    @Transactional(readOnly = true)
    public List<Orden> listarPorUsuario(Integer idUsuario) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");
        return ordenRepository.findByUsuarioIdUsuarioOrderByFechaOrdenDesc(idUsuarioSeguro);
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> listarDtoPorUsuario(Integer idUsuario) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");

        List<Orden> ordenes = ordenRepository.findByUsuarioIdUsuarioOrderByFechaOrdenDesc(idUsuarioSeguro);

        return ordenes.stream().map(orden -> {
            OrdenResponseDTO dto = ordenMapper.toResponse(orden);
            dto.setMetodoPago(obtenerMetodoPago(orden.getIdOrden()));
            return dto;
        }).toList();
    }

    @Transactional(readOnly = true)
    public Orden obtenerPorIdDeUsuario(Integer idOrden, Integer idUsuario) {
        Integer idOrdenSeguro = Objects.requireNonNull(idOrden, "El id de la orden no puede ser null");
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");

        return ordenRepository.findByIdOrdenAndUsuarioIdUsuario(idOrdenSeguro, idUsuarioSeguro)
                .orElseThrow(() -> new RuntimeException("No se encontró la orden solicitada"));
    }

    @Transactional(readOnly = true)
    public OrdenResponseDTO obtenerDtoPorIdDeUsuario(Integer idOrden, Integer idUsuario) {
        Integer idOrdenSeguro = Objects.requireNonNull(idOrden, "El id de la orden no puede ser null");
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");

        Orden orden = ordenRepository.findByIdOrdenAndUsuarioIdUsuario(idOrdenSeguro, idUsuarioSeguro)
                .orElseThrow(() -> new RuntimeException("No se encontró la orden solicitada"));

        OrdenResponseDTO dto = ordenMapper.toResponse(orden);
        dto.setMetodoPago(obtenerMetodoPago(orden.getIdOrden()));

        return dto;
    }

    @Transactional
    public Orden actualizarEstado(Integer idOrden, String nombreEstado) {
        Integer idOrdenSeguro = Objects.requireNonNull(idOrden, "El id de la orden no puede ser null");
        String estadoSeguro = Objects.requireNonNull(nombreEstado, "El nombre del estado no puede ser null").trim();

        Orden orden = ordenRepository.findById(idOrdenSeguro)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        EstadoOrden estado = estadoRepository.findByNombre(estadoSeguro)
                .orElseThrow(() -> new RuntimeException("Estado no válido: " + estadoSeguro));

        orden.setEstado(estado);
        return ordenRepository.save(orden);
    }

    private String obtenerMetodoPago(Integer idOrden) {
        Optional<Transaccion> trxOpt = transaccionRepository.findTopByOrden_IdOrdenOrderByIdTransaccionDesc(idOrden);

        if (trxOpt.isEmpty()) {
            return "No especificado";
        }

        Transaccion trx = trxOpt.get();

        if (trx.getMetodoPago() == null || trx.getMetodoPago().getNombre() == null) {
            return "No especificado";
        }

        return trx.getMetodoPago().getNombre();
    }
}