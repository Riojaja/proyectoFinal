package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Direccion;
import pe.com.punamba.backend_punamba.entity.Ubigeo;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.repository.DireccionRepository;
import pe.com.punamba.backend_punamba.repository.UbigeoRepository;
import pe.com.punamba.backend_punamba.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@Service
public class DireccionService {

    @Autowired private DireccionRepository direccionRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private UbigeoRepository ubigeoRepository;

    @Transactional(readOnly = true)
    public List<Direccion> listarPorUsuario(Integer idUsuario) {
        return direccionRepository.findByUsuarioIdUsuario(idUsuario);
    }

    @Transactional(readOnly = true)
    public Optional<Direccion> buscarPorId(Integer idDireccion) {
        return direccionRepository.findById(idDireccion);
    }

    @Transactional
    public Direccion guardarNueva(Integer idUsuario, String codigoUbigeo, Direccion direccion) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Ubigeo ubigeo = ubigeoRepository.findById(codigoUbigeo)
                .orElseThrow(() -> new RuntimeException("Código de Ubigeo no válido"));

        if (direccion.getEsPrincipal() != null && direccion.getEsPrincipal()) {
            List<Direccion> existentes = direccionRepository.findByUsuarioIdUsuario(idUsuario);
            for (Direccion dir : existentes) {
                dir.setEsPrincipal(false);
            }
            direccionRepository.saveAll(existentes);
        }

        direccion.setUsuario(usuario);
        direccion.setUbigeo(ubigeo);
        
        return direccionRepository.save(direccion);
    }

    @Transactional
    public Direccion actualizar(Integer idDireccion, String codigoUbigeo, Direccion detalles) {
        Direccion direccionDB = direccionRepository.findById(idDireccion)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));

        Ubigeo ubigeo = ubigeoRepository.findById(codigoUbigeo)
                .orElseThrow(() -> new RuntimeException("Código de Ubigeo no válido"));

        if (detalles.getEsPrincipal() != null && detalles.getEsPrincipal()) {
            List<Direccion> existentes = direccionRepository.findByUsuarioIdUsuario(direccionDB.getUsuario().getIdUsuario());
            for (Direccion dir : existentes) {
                if (!dir.getIdDireccion().equals(idDireccion)) {
                    dir.setEsPrincipal(false);
                }
            }
            direccionRepository.saveAll(existentes);
        }

        direccionDB.setNombreDestinatario(detalles.getNombreDestinatario());
        direccionDB.setTelefono(detalles.getTelefono());
        direccionDB.setDireccionLinea1(detalles.getDireccionLinea1());
        direccionDB.setDireccionLinea2(detalles.getDireccionLinea2());
        direccionDB.setEsPrincipal(detalles.getEsPrincipal());
        direccionDB.setUbigeo(ubigeo);

        return direccionRepository.save(direccionDB);
    }

    @Transactional
    public void eliminar(Integer idDireccion) {
        direccionRepository.deleteById(idDireccion);
    }
}