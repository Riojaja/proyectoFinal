package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Favorito;
import pe.com.punamba.backend_punamba.entity.Producto;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.repository.FavoritoRepository;
import pe.com.punamba.backend_punamba.repository.ProductoRepository;
import pe.com.punamba.backend_punamba.repository.UsuarioRepository;

import java.util.List;

@Service
public class FavoritoService {

    @Autowired private FavoritoRepository favoritoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<Favorito> listarPorUsuario(Integer idUsuario) {
        return favoritoRepository.findByUsuarioIdUsuario(idUsuario);
    }

    @Transactional
    public Favorito agregar(Integer idUsuario, Integer idProducto) {
        return favoritoRepository.findByUsuarioIdUsuarioAndProductoIdProducto(idUsuario, idProducto)
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(idUsuario)
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                    Producto producto = productoRepository.findById(idProducto)
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                    Favorito nuevo = new Favorito();
                    nuevo.setUsuario(usuario);
                    nuevo.setProducto(producto);
                    return favoritoRepository.save(nuevo);
                });
    }

    @Transactional
    public void eliminar(Integer idUsuario, Integer idProducto) {
        favoritoRepository.findByUsuarioIdUsuarioAndProductoIdProducto(idUsuario, idProducto)
                .ifPresent(f -> favoritoRepository.delete(f));
    }

    @Transactional(readOnly = true)
    public boolean verificarSiEsFavorito(Integer idUsuario, Integer idProducto) {
        return favoritoRepository.findByUsuarioIdUsuarioAndProductoIdProducto(idUsuario, idProducto).isPresent();
    }
}