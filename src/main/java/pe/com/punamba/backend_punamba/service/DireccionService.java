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
import java.util.Objects;
import java.util.Optional;

@Service
public class DireccionService {

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UbigeoRepository ubigeoRepository;

    @Transactional(readOnly = true)
    public List<Direccion> listarPorUsuario(Integer idUsuario) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");
        return direccionRepository.findByUsuarioIdUsuarioOrderByEsPrincipalDescIdDireccionDesc(idUsuarioSeguro);
    }

    @Transactional(readOnly = true)
    public Optional<Direccion> buscarPorId(Integer idDireccion) {
        Integer idDireccionSeguro = Objects.requireNonNull(idDireccion, "El id de la dirección no puede ser null");
        return direccionRepository.findById(idDireccionSeguro);
    }

    @Transactional
    public Direccion guardarNueva(Integer idUsuario, String codigoUbigeo, Direccion direccion) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");
        String codigoUbigeoSeguro = Objects.requireNonNull(codigoUbigeo, "El código de ubigeo no puede ser null");
        Direccion direccionSegura = Objects.requireNonNull(direccion, "La dirección no puede ser null");

        Usuario usuario = usuarioRepository.findById(idUsuarioSeguro)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Ubigeo ubigeo = ubigeoRepository.findById(codigoUbigeoSeguro)
                .orElseThrow(() -> new RuntimeException("Código de Ubigeo no válido"));

        if (Boolean.TRUE.equals(direccionSegura.getEsPrincipal())) {
            List<Direccion> existentes = direccionRepository
                    .findByUsuarioIdUsuarioOrderByEsPrincipalDescIdDireccionDesc(idUsuarioSeguro);

            for (Direccion dir : existentes) {
                if (Boolean.TRUE.equals(dir.getEsPrincipal())) {
                    dir.setEsPrincipal(false);
                }
            }

            direccionRepository.saveAll(existentes);
        }

        direccionSegura.setUsuario(usuario);
        direccionSegura.setUbigeo(ubigeo);

        if (direccionSegura.getEsPrincipal() == null) {
            direccionSegura.setEsPrincipal(false);
        }

        return direccionRepository.save(direccionSegura);
    }

    @Transactional
    public Direccion actualizar(Integer idDireccion, String codigoUbigeo, Direccion detalles) {
        Integer idDireccionSeguro = Objects.requireNonNull(idDireccion, "El id de la dirección no puede ser null");
        String codigoUbigeoSeguro = Objects.requireNonNull(codigoUbigeo, "El código de ubigeo no puede ser null");
        Direccion detallesSeguro = Objects.requireNonNull(detalles, "Los detalles de la dirección no pueden ser null");

        Direccion direccionDB = direccionRepository.findById(idDireccionSeguro)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));

        Ubigeo ubigeo = ubigeoRepository.findById(codigoUbigeoSeguro)
                .orElseThrow(() -> new RuntimeException("Código de Ubigeo no válido"));

        if (Boolean.TRUE.equals(detallesSeguro.getEsPrincipal())) {
            List<Direccion> existentes = direccionRepository
                    .findByUsuarioIdUsuarioOrderByEsPrincipalDescIdDireccionDesc(direccionDB.getUsuario().getIdUsuario());

            for (Direccion dir : existentes) {
                if (!dir.getIdDireccion().equals(idDireccionSeguro) && Boolean.TRUE.equals(dir.getEsPrincipal())) {
                    dir.setEsPrincipal(false);
                }
            }

            direccionRepository.saveAll(existentes);
        }

        direccionDB.setNombreDestinatario(detallesSeguro.getNombreDestinatario());
        direccionDB.setTelefono(detallesSeguro.getTelefono());
        direccionDB.setDireccionLinea1(detallesSeguro.getDireccionLinea1());
        direccionDB.setDireccionLinea2(detallesSeguro.getDireccionLinea2());
        direccionDB.setEsPrincipal(Boolean.TRUE.equals(detallesSeguro.getEsPrincipal()));
        direccionDB.setUbigeo(ubigeo);

        return direccionRepository.save(direccionDB);
    }

    @Transactional
    public void eliminar(Integer idDireccion) {
        Integer idDireccionSeguro = Objects.requireNonNull(idDireccion, "El id de la dirección no puede ser null");
        direccionRepository.deleteById(idDireccionSeguro);
    }
}