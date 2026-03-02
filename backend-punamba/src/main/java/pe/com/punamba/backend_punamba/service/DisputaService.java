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
import java.util.Optional;

@Service
public class DisputaService {

    @Autowired private DisputaRepository disputaRepository;
    @Autowired private OrdenDetalleRepository ordenDetalleRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Disputa> listarPorUsuario(Integer idUsuario) {
        return disputaRepository.findByUsuarioIdUsuarioOrderByFechaAperturaDesc(idUsuario);
    }

    @Transactional(readOnly = true)
    public List<Disputa> listarPorEstado(Disputa.EstadoDisputa estado) {
        return disputaRepository.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public Optional<Disputa> buscarPorId(Integer id) {
        return disputaRepository.findById(id);
    }

    @Transactional
    public Disputa abrirDisputa(Integer idUsuario, Integer idOrdenDetalle, Disputa datosDisputa) {
        if (disputaRepository.findByOrdenDetalleIdOrdenDetalle(idOrdenDetalle).isPresent()) {
            throw new RuntimeException("Ya existe una disputa para este artículo.");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        OrdenDetalle detalle = ordenDetalleRepository.findById(idOrdenDetalle)
                .orElseThrow(() -> new RuntimeException("Detalle de orden no encontrado"));

        datosDisputa.setUsuario(usuario);
        datosDisputa.setOrdenDetalle(detalle);
        datosDisputa.setEstado(Disputa.EstadoDisputa.abierta);
        
        return disputaRepository.save(datosDisputa);
    }

    @Transactional
    public Disputa resolverDisputa(Integer idDisputa, Disputa.EstadoDisputa nuevoEstado) {
        Disputa disputa = disputaRepository.findById(idDisputa)
                .orElseThrow(() -> new RuntimeException("Disputa no encontrada"));
        
        disputa.setEstado(nuevoEstado);
        
        if (nuevoEstado == Disputa.EstadoDisputa.resuelta_reembolso || nuevoEstado == Disputa.EstadoDisputa.resuelta_rechazada) {
            disputa.setFechaResolucion(LocalDateTime.now());
        }
        
        return disputaRepository.save(disputa);
    }
}