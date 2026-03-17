package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Disputa;
import pe.com.punamba.backend_punamba.entity.OrdenDetalle;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.repository.DisputaRepository;
import pe.com.punamba.backend_punamba.repository.OrdenDetalleRepository;
import pe.com.punamba.backend_punamba.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DisputaService {

    @Autowired
    private DisputaRepository disputaRepository;

    @Autowired
    private OrdenDetalleRepository ordenDetalleRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Disputa> listarPorUsuario(Integer idUsuario) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");
        return disputaRepository.findByUsuarioIdUsuarioOrderByFechaAperturaDesc(idUsuarioSeguro);
    }

    @Transactional(readOnly = true)
    public List<Disputa> listarPorEstado(Disputa.EstadoDisputa estado) {
        Disputa.EstadoDisputa estadoSeguro = Objects.requireNonNull(estado, "El estado no puede ser null");
        return disputaRepository.findByEstado(estadoSeguro);
    }

    @Transactional(readOnly = true)
    public Optional<Disputa> buscarPorId(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id de la disputa no puede ser null");
        return disputaRepository.findById(idSeguro);
    }

    @Transactional
    public Disputa abrirDisputa(Integer idUsuario, Integer idOrdenDetalle, Disputa datosDisputa) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");
        Integer idOrdenDetalleSeguro = Objects.requireNonNull(idOrdenDetalle, "El id del detalle de orden no puede ser null");
        Disputa disputaSegura = Objects.requireNonNull(datosDisputa, "Los datos de la disputa no pueden ser null");

        if (disputaRepository.findByOrdenDetalleIdOrdenDetalle(idOrdenDetalleSeguro).isPresent()) {
            throw new RuntimeException("Ya existe una disputa para este artículo.");
        }

        Usuario usuario = usuarioRepository.findById(idUsuarioSeguro)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        OrdenDetalle detalle = ordenDetalleRepository.findById(idOrdenDetalleSeguro)
                .orElseThrow(() -> new RuntimeException("Detalle de orden no encontrado"));

        disputaSegura.setUsuario(usuario);
        disputaSegura.setOrdenDetalle(detalle);
        disputaSegura.setEstado(Disputa.EstadoDisputa.abierta);

        return disputaRepository.save(disputaSegura);
    }

    @Transactional
    public Disputa resolverDisputa(Integer idDisputa, Disputa.EstadoDisputa nuevoEstado) {
        Integer idDisputaSeguro = Objects.requireNonNull(idDisputa, "El id de la disputa no puede ser null");
        Disputa.EstadoDisputa estadoSeguro = Objects.requireNonNull(nuevoEstado, "El nuevo estado no puede ser null");

        Disputa disputa = disputaRepository.findById(idDisputaSeguro)
                .orElseThrow(() -> new RuntimeException("Disputa no encontrada"));

        disputa.setEstado(estadoSeguro);

        if (estadoSeguro == Disputa.EstadoDisputa.resuelta_reembolso
                || estadoSeguro == Disputa.EstadoDisputa.resuelta_rechazada) {
            disputa.setFechaResolucion(LocalDateTime.now());
        }

        return disputaRepository.save(disputa);
    }
}