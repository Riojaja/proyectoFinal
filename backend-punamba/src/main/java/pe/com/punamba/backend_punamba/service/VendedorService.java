package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.*;
import pe.com.punamba.backend_punamba.repository.*;

import java.util.List;
import java.util.Optional;
import java.util.HashSet;

@Service
public class VendedorService {

    @Autowired 
    private VendedorRepository vendedorRepository;
    
    @Autowired 
    private RolRepository rolRepository;

    @Autowired 
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Vendedor> listarTodos() {
        return vendedorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Vendedor> buscarPorId(Integer id) {
        return vendedorRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Vendedor> buscarPorIdUsuario(Integer idUsuario) {
        return vendedorRepository.findByUsuarioIdUsuario(idUsuario);
    }

    @Transactional
    public Vendedor guardarNuevo(Vendedor vendedor) {
        Usuario usuarioFinal;
        Usuario usuarioEnviado = vendedor.getUsuario();

        if (usuarioEnviado.getIdUsuario() != null) {
            usuarioFinal = usuarioRepository.findById(usuarioEnviado.getIdUsuario())
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
        usuarioFinal.getRoles().add(rolVendedor);

        vendedor.setUsuario(usuarioFinal);
        
        if (vendedor.getEstado() == null) {
            vendedor.setEstado(Vendedor.EstadoVendedor.pendiente);
        }

        return vendedorRepository.save(vendedor);
    }

    @Transactional
    public Vendedor actualizar(Vendedor vendedor) {
        return vendedorRepository.save(vendedor);
    }

    @Transactional
    public void eliminar(Integer id) {
        vendedorRepository.findById(id).ifPresent(v -> {
            v.setEstado(Vendedor.EstadoVendedor.inactivo);
            vendedorRepository.save(v);
        });
    }

    public boolean existeRuc(String ruc) { 
        return vendedorRepository.existsByRuc(ruc); 
    }
    
    public boolean existeNombreTienda(String nombre) { 
        return vendedorRepository.existsByNombreTienda(nombre); 
    }

    public boolean usuarioYaEsVendedor(Integer idUsuario) { 
        return vendedorRepository.existsByUsuarioIdUsuario(idUsuario); 
    }
}