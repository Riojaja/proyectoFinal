package pe.com.punamba.backend_punamba.service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.punamba.backend_punamba.dto.AdminVendedorModeracionDTO;
import pe.com.punamba.backend_punamba.entity.MetodoEnvio;
import pe.com.punamba.backend_punamba.entity.OrdenDetalle;
import pe.com.punamba.backend_punamba.entity.Rol;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.entity.Vendedor;
import pe.com.punamba.backend_punamba.repository.MetodoEnvioRepository;
import pe.com.punamba.backend_punamba.repository.OrdenDetalleRepository;
import pe.com.punamba.backend_punamba.repository.RolRepository;
import pe.com.punamba.backend_punamba.repository.UsuarioRepository;
import pe.com.punamba.backend_punamba.repository.VendedorRepository;

@Service
public class VendedorService {

    @Autowired
    private VendedorRepository vendedorRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OrdenDetalleRepository ordenDetalleRepository;

    @Autowired
    private MetodoEnvioRepository metodoEnvioRepository;

    @Transactional(readOnly = true)
    public List<Vendedor> listarTodos() {
        return vendedorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Vendedor> buscarPorId(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id no puede ser null");
        return vendedorRepository.findById(idSeguro);
    }

    @Transactional(readOnly = true)
    public Optional<Vendedor> buscarPorIdUsuario(Integer idUsuario) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El idUsuario no puede ser null");
        return vendedorRepository.findByUsuarioIdUsuario(idUsuarioSeguro);
    }

    @Transactional
    public Vendedor guardarNuevo(Vendedor vendedor) {
        Vendedor vendedorSeguro = Objects.requireNonNull(vendedor, "El vendedor no puede ser null");
        Usuario usuarioEnviado = Objects.requireNonNull(vendedorSeguro.getUsuario(), "El usuario del vendedor no puede ser null");

        Usuario usuarioFinal;

        if (usuarioEnviado.getIdUsuario() != null) {
            Integer idUsuarioSeguro = Objects.requireNonNull(usuarioEnviado.getIdUsuario(), "El idUsuario no puede ser null");
            usuarioFinal = usuarioRepository.findById(idUsuarioSeguro)
                    .orElseThrow(() -> new RuntimeException("Error: El usuario no existe."));
        } else {
            if (usuarioEnviado.getRoles() == null) {
                usuarioEnviado.setRoles(new HashSet<>());
            }
            usuarioEnviado.setEstado(Usuario.EstadoUsuario.activo);
            usuarioFinal = usuarioRepository.save(usuarioEnviado);
        }

        Rol rolVendedor = rolRepository.findByNombre("VENDEDOR")
                .orElseThrow(() -> new RuntimeException("Error: Rol VENDEDOR no configurado en BD."));

        if (usuarioFinal.getRoles() == null) {
            usuarioFinal.setRoles(new HashSet<>());
        }

        boolean yaTieneRolVendedor = usuarioFinal.getRoles().stream()
                .anyMatch(r -> "VENDEDOR".equalsIgnoreCase(r.getNombre()));

        if (!yaTieneRolVendedor) {
            usuarioFinal.getRoles().add(rolVendedor);
        }

        usuarioRepository.save(usuarioFinal);

        vendedorSeguro.setUsuario(usuarioFinal);

        if (vendedorSeguro.getEstado() == null) {
            vendedorSeguro.setEstado(Vendedor.EstadoVendedor.pendiente);
        }

        return vendedorRepository.save(vendedorSeguro);
    }

    @Transactional
    public Vendedor actualizar(Vendedor vendedor) {
        Vendedor vendedorSeguro = Objects.requireNonNull(vendedor, "El vendedor no puede ser null");
        return vendedorRepository.save(vendedorSeguro);
    }

    @Transactional
    public void eliminar(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id no puede ser null");

        vendedorRepository.findById(idSeguro).ifPresent(v -> {
            v.setEstado(Vendedor.EstadoVendedor.inactivo);
            vendedorRepository.save(v);
        });
    }

    public boolean existeRuc(String ruc) {
        String rucSeguro = Objects.requireNonNull(ruc, "El RUC no puede ser null");
        return vendedorRepository.existsByRuc(rucSeguro);
    }

    public boolean existeNombreTienda(String nombre) {
        String nombreSeguro = Objects.requireNonNull(nombre, "El nombre no puede ser null");
        return vendedorRepository.existsByNombreTienda(nombreSeguro);
    }

    public boolean usuarioYaEsVendedor(Integer idUsuario) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El idUsuario no puede ser null");
        return vendedorRepository.existsByUsuarioIdUsuario(idUsuarioSeguro);
    }

    @Transactional(readOnly = true)
    public List<AdminVendedorModeracionDTO> listarParaModeracion() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<Vendedor> lista = vendedorRepository.findAll();
        List<AdminVendedorModeracionDTO> out = new ArrayList<>();

        for (Vendedor v : lista) {
            AdminVendedorModeracionDTO dto = new AdminVendedorModeracionDTO();

            dto.setIdVendedor(v.getIdVendedor());
            dto.setNombreEmpresa(v.getNombreTienda());
            dto.setRuc(v.getRuc());
            dto.setEstado(v.getEstado() != null ? v.getEstado().name() : "pendiente");
            dto.setTiendas(v.getNombreTienda());

            if (v.getUsuario() != null) {
                dto.setEmail(v.getUsuario().getEmail());
                dto.setTelefono(v.getUsuario().getTelefono());
            } else {
                dto.setEmail("");
                dto.setTelefono("");
            }

            if (v.getFechaRegistro() != null) {
                dto.setFechaSolicitud(v.getFechaRegistro().format(fmt));
            } else {
                dto.setFechaSolicitud("");
            }

            dto.setCalificacion(v.getCalificacion() != null ? v.getCalificacion().doubleValue() : 0.0);
            dto.setTotalVentas(v.getTotalVentas() != null ? BigDecimal.valueOf(v.getTotalVentas()) : BigDecimal.ZERO);

            out.add(dto);
        }

        return out;
    }

    @Transactional
    public Vendedor aprobarSolicitud(Integer idVendedor) {
        Integer idVendedorSeguro = Objects.requireNonNull(idVendedor, "El idVendedor no puede ser null");

        Vendedor vendedor = vendedorRepository.findById(idVendedorSeguro)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        vendedor.setEstado(Vendedor.EstadoVendedor.activo);

        Usuario usuario = vendedor.getUsuario();
        if (usuario == null) {
            throw new RuntimeException("El vendedor no tiene usuario asociado");
        }

        Rol rolVendedor = rolRepository.findByNombre("VENDEDOR")
                .orElseThrow(() -> new RuntimeException("Error: Rol VENDEDOR no configurado en BD."));

        if (usuario.getRoles() == null) {
            usuario.setRoles(new HashSet<>());
        }

        boolean yaTieneRolVendedor = usuario.getRoles().stream()
                .anyMatch(r -> "VENDEDOR".equalsIgnoreCase(r.getNombre()));

        if (!yaTieneRolVendedor) {
            usuario.getRoles().add(rolVendedor);
        }

        usuarioRepository.save(usuario);

        return vendedorRepository.save(vendedor);
    }

    @Transactional
    public Vendedor rechazarSolicitud(Integer idVendedor) {
        Integer idVendedorSeguro = Objects.requireNonNull(idVendedor, "El idVendedor no puede ser null");

        Vendedor vendedor = vendedorRepository.findById(idVendedorSeguro)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        vendedor.setEstado(Vendedor.EstadoVendedor.inactivo);
        return vendedorRepository.save(vendedor);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPedidosPanel(Integer idUsuario) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El idUsuario no puede ser null");

        Vendedor vendedor = vendedorRepository.findByUsuarioIdUsuario(idUsuarioSeguro)
                .orElseThrow(() -> new RuntimeException("No tienes perfil de vendedor"));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<OrdenDetalle> detalles = ordenDetalleRepository.findByVendedorIdVendedor(vendedor.getIdVendedor());

        return detalles.stream()
                .map(det -> {
                    Map<String, Object> row = new HashMap<>();

                    String clienteNombre = det.getOrden().getUsuario().getNombre() + " "
                            + det.getOrden().getUsuario().getApellido();

                    String direccionEnvio = det.getOrden().getDireccion().getDireccionLinea1();
                    if (det.getOrden().getDireccion().getUbigeo() != null) {
                        direccionEnvio += " - "
                                + det.getOrden().getDireccion().getUbigeo().getDistrito() + ", "
                                + det.getOrden().getDireccion().getUbigeo().getProvincia() + ", "
                                + det.getOrden().getDireccion().getUbigeo().getDepartamento();
                    }

                    BigDecimal comision = det.getComisionPlataforma() != null
                            ? det.getComisionPlataforma()
                            : BigDecimal.ZERO;

                    BigDecimal subtotal = det.getSubtotal() != null
                            ? det.getSubtotal()
                            : BigDecimal.ZERO;

                    row.put("idPedido", det.getIdOrdenDetalle());
                    row.put("fechaOrden", det.getOrden().getFechaOrden() != null
                            ? det.getOrden().getFechaOrden().format(fmt)
                            : "");
                    row.put("nroOrden", det.getOrden().getNumeroOrden());
                    row.put("producto", det.getVariante().getProducto().getNombre());
                    row.put("sku", det.getVariante().getSku());
                    row.put("cantidad", det.getCantidad());
                    row.put("precioSnap", det.getPrecioUnitarioSnapshot());
                    row.put("subtotalSnap", subtotal);
                    row.put("estadoPago", det.getOrden().getEstado() != null
                            ? det.getOrden().getEstado().getNombre()
                            : "PENDIENTE");
                    row.put("estadoEnvio", det.getEstadoEnvio() != null && !det.getEstadoEnvio().isBlank()
                            ? det.getEstadoEnvio().toUpperCase()
                            : "PROCESANDO");
                    row.put("clienteNombre", clienteNombre);
                    row.put("clienteTelefono", det.getOrden().getDireccion().getTelefono());
                    row.put("emailPublico", det.getOrden().getUsuario().getEmail());
                    row.put("direccionEnvio", direccionEnvio);
                    row.put("comisionPlataforma", comision);
                    row.put("montoPagarEstimado", subtotal.subtract(comision));
                    row.put("numeroSeguimiento", det.getNumeroSeguimiento());
                    row.put("metodoEnvio", det.getMetodoEnvio() != null ? det.getMetodoEnvio().getNombre() : "");

                    return row;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> actualizarEnvioPedido(Integer idUsuario, Integer idPedido, Map<String, Object> payload) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El idUsuario no puede ser null");
        Integer idPedidoSeguro = Objects.requireNonNull(idPedido, "El idPedido no puede ser null");
        Map<String, Object> payloadSeguro = Objects.requireNonNull(payload, "El payload no puede ser null");

        Vendedor vendedor = vendedorRepository.findByUsuarioIdUsuario(idUsuarioSeguro)
                .orElseThrow(() -> new RuntimeException("No tienes perfil de vendedor"));

        OrdenDetalle detalle = ordenDetalleRepository.findById(idPedidoSeguro)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (!detalle.getVendedor().getIdVendedor().equals(vendedor.getIdVendedor())) {
            throw new RuntimeException("No tienes permiso para actualizar este pedido");
        }

        String estadoOperativo = payloadSeguro.get("estadoOperativo") != null
                ? payloadSeguro.get("estadoOperativo").toString().trim().toUpperCase()
                : "PROCESANDO";

        String numeroSeguimiento = payloadSeguro.get("numeroSeguimiento") != null
                ? payloadSeguro.get("numeroSeguimiento").toString().trim()
                : "";

        Integer metodoEnvioId = null;
        if (payloadSeguro.get("metodoEnvioId") != null && !payloadSeguro.get("metodoEnvioId").toString().isBlank()) {
            metodoEnvioId = Integer.valueOf(payloadSeguro.get("metodoEnvioId").toString());
        }

        detalle.setEstadoEnvio(estadoOperativo);
        detalle.setNumeroSeguimiento(numeroSeguimiento);

        if (metodoEnvioId != null) {
            MetodoEnvio metodoEnvio = metodoEnvioRepository.findById(metodoEnvioId)
                    .orElseThrow(() -> new RuntimeException("Método de envío no encontrado"));
            detalle.setMetodoEnvio(metodoEnvio);
        }

        ordenDetalleRepository.save(detalle);

        Map<String, Object> resp = new HashMap<>();
        resp.put("mensaje", "Envío actualizado correctamente");
        resp.put("idPedido", detalle.getIdOrdenDetalle());
        resp.put("estadoEnvio", detalle.getEstadoEnvio());
        resp.put("numeroSeguimiento", detalle.getNumeroSeguimiento());
        resp.put("metodoEnvio", detalle.getMetodoEnvio() != null ? detalle.getMetodoEnvio().getNombre() : "");

        return resp;
    }
}