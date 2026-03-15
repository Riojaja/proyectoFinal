package pe.com.punamba.backend_punamba.service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.punamba.backend_punamba.entity.Rol;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.mapper.UsuarioMapper;
import pe.com.punamba.backend_punamba.repository.RolRepository;
import pe.com.punamba.backend_punamba.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Autowired
    private RolRepository rolRepository;

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id no puede ser null");
        return usuarioRepository.findById(idSeguro);
    }

    @Transactional
    public Usuario actualizarPerfil(Integer id, Usuario detalles) {
        Integer idSeguro = Objects.requireNonNull(id, "El id no puede ser null");
        Usuario detallesSeguro = Objects.requireNonNull(detalles, "Los detalles no pueden ser null");

        Usuario usuarioExistente = Objects.requireNonNull(
                usuarioRepository.findById(idSeguro)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado")),
                "El usuario existente no puede ser null"
        );

        usuarioMapper.updateEntityFromDto(
                Objects.requireNonNull(detallesSeguro, "Los detalles no pueden ser null"),
                Objects.requireNonNull(usuarioExistente, "El usuario existente no puede ser null")
        );

        return usuarioRepository.save(
                Objects.requireNonNull(usuarioExistente, "El usuario existente no puede ser null")
        );
    }

    @Transactional
    public void eliminar(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id no puede ser null");
        usuarioRepository.deleteById(idSeguro);
    }

    public boolean existeEmail(String email) {
        String emailSeguro = Objects.requireNonNull(email, "El email no puede ser null");
        return usuarioRepository.existsByEmail(emailSeguro);
    }

    public boolean existeDocumento(String numeroDocumento) {
        String numeroDocumentoSeguro = Objects.requireNonNull(numeroDocumento, "El número de documento no puede ser null");
        return usuarioRepository.existsByNumeroDocumento(numeroDocumentoSeguro);
    }

    @Transactional
    public Usuario actualizarRoles(Integer idUsuario, List<String> rolesNuevos) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El idUsuario no puede ser null");
        List<String> rolesNuevosSeguro = Objects.requireNonNull(rolesNuevos, "La lista de roles no puede ser null");

        Usuario usuario = Objects.requireNonNull(
                usuarioRepository.findById(idUsuarioSeguro)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado")),
                "El usuario no puede ser null"
        );

        Set<Rol> roles = new HashSet<>();

        for (String nombreRol : rolesNuevosSeguro) {
            String nombreRolSeguro = Objects.requireNonNull(nombreRol, "El nombre del rol no puede ser null");

            Rol rol = rolRepository.findByNombre(nombreRolSeguro.toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + nombreRolSeguro));
            roles.add(rol);
        }

        usuario.setRoles(roles);
        return usuarioRepository.save(
                Objects.requireNonNull(usuario, "El usuario no puede ser null")
        );
    }
}