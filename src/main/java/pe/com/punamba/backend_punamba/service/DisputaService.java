package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.dto.DisputaMensajeRequestDTO;
import pe.com.punamba.backend_punamba.dto.DisputaResolverRequestDTO;
import pe.com.punamba.backend_punamba.entity.Disputa;
import pe.com.punamba.backend_punamba.entity.DisputaMensaje;
import pe.com.punamba.backend_punamba.entity.EstadoOrden;
import pe.com.punamba.backend_punamba.entity.Orden;
import pe.com.punamba.backend_punamba.entity.OrdenDetalle;
import pe.com.punamba.backend_punamba.entity.Transaccion;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.repository.DisputaMensajeRepository;
import pe.com.punamba.backend_punamba.repository.DisputaRepository;
import pe.com.punamba.backend_punamba.repository.EstadoOrdenRepository;
import pe.com.punamba.backend_punamba.repository.OrdenDetalleRepository;
import pe.com.punamba.backend_punamba.repository.OrdenRepository;
import pe.com.punamba.backend_punamba.repository.TransaccionRepository;
import pe.com.punamba.backend_punamba.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class DisputaService {

    @Autowired private DisputaRepository disputaRepository;
    @Autowired private DisputaMensajeRepository disputaMensajeRepository;
    @Autowired private OrdenDetalleRepository ordenDetalleRepository;
    @Autowired private OrdenRepository ordenRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EstadoOrdenRepository estadoOrdenRepository;
    @Autowired private TransaccionRepository transaccionRepository;

    @Transactional(readOnly = true)
    public List<Disputa> listarMisDisputas(Usuario actor) {
        Usuario actorSeguro = Objects.requireNonNull(actor, "El usuario autenticado no puede ser null");

        if (esAdmin(actorSeguro)) {
            return disputaRepository.findAllByOrderByFechaAperturaDesc();
        }

        if (esVendedor(actorSeguro)) {
            return disputaRepository.findByOrdenDetalleVendedorUsuarioIdUsuarioOrderByFechaAperturaDesc(actorSeguro.getIdUsuario());
        }

        return disputaRepository.findByUsuarioIdUsuarioOrderByFechaAperturaDesc(actorSeguro.getIdUsuario());
    }

    @Transactional(readOnly = true)
    public List<Disputa> listarTodasAdmin(Usuario actor) {
        validarAdmin(actor);
        return disputaRepository.findAllByOrderByFechaAperturaDesc();
    }

    @Transactional(readOnly = true)
    public List<Disputa> listarPorEstado(Usuario actor, Disputa.EstadoDisputa estado) {
        validarAdmin(actor);
        Disputa.EstadoDisputa estadoSeguro = Objects.requireNonNull(estado, "El estado no puede ser null");
        return disputaRepository.findByEstadoOrderByFechaAperturaDesc(estadoSeguro);
    }

    @Transactional(readOnly = true)
    public Disputa obtenerPorIdParaActor(Integer idDisputa, Usuario actor) {
        Integer idSeguro = Objects.requireNonNull(idDisputa, "El id de la disputa no puede ser null");
        Usuario actorSeguro = Objects.requireNonNull(actor, "El usuario autenticado no puede ser null");

        Disputa disputa = disputaRepository.findById(idSeguro)
                .orElseThrow(() -> new RuntimeException("Disputa no encontrada"));

        validarAccesoLectura(actorSeguro, disputa);
        return disputa;
    }

    @Transactional
    public Disputa abrirDisputa(Usuario actor, Integer idOrdenDetalle, Disputa datosDisputa) {
        Usuario actorSeguro = Objects.requireNonNull(actor, "El usuario autenticado no puede ser null");
        Integer idOrdenDetalleSeguro = Objects.requireNonNull(idOrdenDetalle, "El id del detalle no puede ser null");
        Disputa disputaSegura = Objects.requireNonNull(datosDisputa, "Los datos de la disputa no pueden ser null");

        if (disputaRepository.findByOrdenDetalleIdOrdenDetalle(idOrdenDetalleSeguro).isPresent()) {
            throw new RuntimeException("Ya existe una disputa para este artículo.");
        }

        Usuario usuario = usuarioRepository.findById(actorSeguro.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        OrdenDetalle detalle = ordenDetalleRepository.findById(idOrdenDetalleSeguro)
                .orElseThrow(() -> new RuntimeException("Detalle de orden no encontrado"));

        boolean esDuenoDelPedido = detalle.getOrden() != null
                && detalle.getOrden().getUsuario() != null
                && Objects.equals(detalle.getOrden().getUsuario().getIdUsuario(), usuario.getIdUsuario());

        if (!esAdmin(usuario) && !esDuenoDelPedido) {
            throw new RuntimeException("No puedes abrir una disputa sobre un artículo que no te pertenece.");
        }

        disputaSegura.setUsuario(usuario);
        disputaSegura.setOrdenDetalle(detalle);
        disputaSegura.setEstado(Disputa.EstadoDisputa.abierta);
        disputaSegura.setMontoReembolso(BigDecimal.ZERO);
        disputaSegura.setFechaResolucion(null);
        disputaSegura.setFechaCierre(null);

        detalle.setEstadoPostventa(OrdenDetalle.EstadoPostventa.en_disputa);
        detalle.setMontoReembolsado(BigDecimal.ZERO);
        ordenDetalleRepository.save(detalle);

        return disputaRepository.save(disputaSegura);
    }

    @Transactional
    public Disputa resolverDisputa(Usuario actor, Integer idDisputa, DisputaResolverRequestDTO request) {
        validarAdmin(actor);

        Integer idSeguro = Objects.requireNonNull(idDisputa, "El id de la disputa no puede ser null");
        DisputaResolverRequestDTO requestSeguro = Objects.requireNonNull(request, "La solicitud no puede ser null");
        Disputa.EstadoDisputa nuevoEstado = Objects.requireNonNull(requestSeguro.getNuevoEstado(), "El nuevo estado es obligatorio");

        Disputa disputa = disputaRepository.findById(idSeguro)
                .orElseThrow(() -> new RuntimeException("Disputa no encontrada"));

        OrdenDetalle detalle = disputa.getOrdenDetalle();
        if (detalle == null) {
            throw new RuntimeException("La disputa no tiene detalle de orden asociado.");
        }

        disputa.setEstado(nuevoEstado);
        disputa.setObservacionAdmin(normalizarTexto(requestSeguro.getObservacionAdmin()));
        disputa.setResolucionFinal(normalizarTexto(requestSeguro.getResolucionFinal()));

        switch (nuevoEstado) {
            case abierta:
            case en_revision:
            case en_negociacion:
                disputa.setMontoReembolso(BigDecimal.ZERO);
                disputa.setFechaResolucion(null);
                disputa.setFechaCierre(null);
                detalle.setEstadoPostventa(OrdenDetalle.EstadoPostventa.en_disputa);
                detalle.setMontoReembolsado(BigDecimal.ZERO);
                break;

            case resuelta_reembolso_total:
                disputa.setMontoReembolso(detalle.getSubtotal());
                disputa.setFechaResolucion(LocalDateTime.now());
                disputa.setFechaCierre(LocalDateTime.now());
                detalle.setEstadoPostventa(OrdenDetalle.EstadoPostventa.reembolsado);
                detalle.setMontoReembolsado(detalle.getSubtotal());
                break;

            case resuelta_reembolso_parcial:
                BigDecimal montoParcial = requestSeguro.getMontoReembolso();
                if (montoParcial == null || montoParcial.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("Debes enviar un monto de reembolso parcial válido.");
                }
                if (montoParcial.compareTo(detalle.getSubtotal()) > 0) {
                    throw new RuntimeException("El monto parcial no puede ser mayor al subtotal del artículo.");
                }

                disputa.setMontoReembolso(montoParcial);
                disputa.setFechaResolucion(LocalDateTime.now());
                disputa.setFechaCierre(LocalDateTime.now());
                detalle.setEstadoPostventa(OrdenDetalle.EstadoPostventa.reembolsado);
                detalle.setMontoReembolsado(montoParcial);
                break;

            case resuelta_rechazada:
                disputa.setMontoReembolso(BigDecimal.ZERO);
                disputa.setFechaResolucion(LocalDateTime.now());
                disputa.setFechaCierre(LocalDateTime.now());
                detalle.setEstadoPostventa(OrdenDetalle.EstadoPostventa.rechazado);
                detalle.setMontoReembolsado(BigDecimal.ZERO);
                break;

            case cerrada:
                disputa.setFechaCierre(LocalDateTime.now());

                if (disputa.getFechaResolucion() == null) {
                    disputa.setFechaResolucion(LocalDateTime.now());
                }

                if (detalle.getEstadoPostventa() == OrdenDetalle.EstadoPostventa.en_disputa) {
                    detalle.setEstadoPostventa(OrdenDetalle.EstadoPostventa.normal);
                }

                break;

            default:
                throw new RuntimeException("Estado de disputa no soportado.");
        }

        ordenDetalleRepository.save(detalle);
        Disputa actualizada = disputaRepository.save(disputa);

        actualizarEstadoGlobalOrdenSiCorresponde(detalle.getOrden());

        return actualizada;
    }

    @Transactional(readOnly = true)
    public List<DisputaMensaje> listarMensajes(Usuario actor, Integer idDisputa) {
        Disputa disputa = obtenerPorIdParaActor(idDisputa, actor);
        return disputaMensajeRepository.findByDisputaIdDisputaOrderByFechaEnvioAsc(disputa.getIdDisputa());
    }

    @Transactional
    public DisputaMensaje enviarMensaje(Usuario actor, Integer idDisputa, DisputaMensajeRequestDTO request) {
        Usuario actorSeguro = Objects.requireNonNull(actor, "El usuario autenticado no puede ser null");
        Integer idSeguro = Objects.requireNonNull(idDisputa, "El id de la disputa no puede ser null");
        DisputaMensajeRequestDTO requestSeguro = Objects.requireNonNull(request, "El mensaje no puede ser null");

        Disputa disputa = disputaRepository.findById(idSeguro)
                .orElseThrow(() -> new RuntimeException("Disputa no encontrada"));

        validarAccesoLectura(actorSeguro, disputa);

        String texto = requestSeguro.getMensaje() != null ? requestSeguro.getMensaje().trim() : "";
        if (texto.isBlank()) {
            throw new RuntimeException("El mensaje no puede estar vacío.");
        }

        DisputaMensaje mensaje = new DisputaMensaje();
        mensaje.setDisputa(disputa);
        mensaje.setUsuario(actorSeguro);
        mensaje.setRolEmisor(resolverRol(actorSeguro, disputa));
        mensaje.setMensaje(texto);
        mensaje.setAdjuntoUrl(normalizarTexto(requestSeguro.getAdjuntoUrl()));

        return disputaMensajeRepository.save(mensaje);
    }

    private void actualizarEstadoGlobalOrdenSiCorresponde(Orden orden) {
        if (orden == null || orden.getIdOrden() == null) {
            return;
        }

        List<OrdenDetalle> detalles = ordenDetalleRepository.findByOrdenIdOrden(orden.getIdOrden());
        if (detalles == null || detalles.isEmpty()) {
            return;
        }

        boolean todosReembolsados = detalles.stream()
                .allMatch(d -> d.getEstadoPostventa() == OrdenDetalle.EstadoPostventa.reembolsado);

        if (!todosReembolsados) {
            return;
        }

        EstadoOrden estadoReembolsado = estadoOrdenRepository.findByNombre("REEMBOLSADO")
                .orElse(null);

        if (estadoReembolsado != null) {
            orden.setEstado(estadoReembolsado);
            ordenRepository.save(orden);
        }

        transaccionRepository.findTopByOrden_IdOrdenOrderByIdTransaccionDesc(orden.getIdOrden())
                .ifPresent(trx -> {
                    trx.setEstado(Transaccion.EstadoTransaccion.reembolsado);
                    transaccionRepository.save(trx);
                });
    }

    private void validarAccesoLectura(Usuario actor, Disputa disputa) {
        if (esAdmin(actor)) {
            return;
        }

        boolean esCliente = disputa.getUsuario() != null
                && Objects.equals(disputa.getUsuario().getIdUsuario(), actor.getIdUsuario());

        boolean esVendedor = disputa.getOrdenDetalle() != null
                && disputa.getOrdenDetalle().getVendedor() != null
                && disputa.getOrdenDetalle().getVendedor().getUsuario() != null
                && Objects.equals(
                        disputa.getOrdenDetalle().getVendedor().getUsuario().getIdUsuario(),
                        actor.getIdUsuario()
                );

        if (!esCliente && !esVendedor) {
            throw new RuntimeException("No tienes permisos para acceder a esta disputa.");
        }
    }

    private void validarAdmin(Usuario actor) {
        if (actor == null || !esAdmin(actor)) {
            throw new RuntimeException("Acceso denegado. Solo el administrador puede realizar esta acción.");
        }
    }

    private DisputaMensaje.RolEmisor resolverRol(Usuario actor, Disputa disputa) {
        if (esAdmin(actor)) {
            return DisputaMensaje.RolEmisor.admin;
        }

        boolean esVendedor = disputa.getOrdenDetalle() != null
                && disputa.getOrdenDetalle().getVendedor() != null
                && disputa.getOrdenDetalle().getVendedor().getUsuario() != null
                && Objects.equals(
                        disputa.getOrdenDetalle().getVendedor().getUsuario().getIdUsuario(),
                        actor.getIdUsuario()
                );

        if (esVendedor) {
            return DisputaMensaje.RolEmisor.vendedor;
        }

        return DisputaMensaje.RolEmisor.cliente;
    }

    private boolean esAdmin(Usuario usuario) {
        return usuario.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));
    }

    private boolean esVendedor(Usuario usuario) {
        return usuario.getAuthorities().stream()
                .anyMatch(a -> "ROLE_VENDEDOR".equalsIgnoreCase(a.getAuthority()));
    }

    private String normalizarTexto(String valor) {
        if (valor == null) return null;
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}