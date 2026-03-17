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
import java.util.Objects;
import java.util.Optional;

@Service
public class FavoritoService {

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<Favorito> listarPorUsuario(Integer idUsuario) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");
        return favoritoRepository.findByUsuarioIdUsuario(idUsuarioSeguro);
    }

    @Transactional
    public Favorito agregar(Integer idUsuario, Integer idProducto) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");
        Integer idProductoSeguro = Objects.requireNonNull(idProducto, "El id del producto no puede ser null");

        return favoritoRepository.findByUsuarioIdUsuarioAndProductoIdProducto(idUsuarioSeguro, idProductoSeguro)
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(idUsuarioSeguro)
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                    Producto producto = productoRepository.findById(idProductoSeguro)
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                    Favorito nuevo = new Favorito();
                    nuevo.setUsuario(usuario);
                    nuevo.setProducto(producto);
                    return favoritoRepository.save(nuevo);
                });
    }

    @Transactional
    public void eliminar(Integer idUsuario, Integer idProducto) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");
        Integer idProductoSeguro = Objects.requireNonNull(idProducto, "El id del producto no puede ser null");

        Optional<Favorito> favoritoOpt = favoritoRepository
                .findByUsuarioIdUsuarioAndProductoIdProducto(idUsuarioSeguro, idProductoSeguro);

        if (favoritoOpt.isPresent()) {
            Favorito favorito = Objects.requireNonNull(favoritoOpt.get(), "El favorito no puede ser null");
            favoritoRepository.delete(favorito);
        }
    }

    @Transactional(readOnly = true)
    public boolean verificarSiEsFavorito(Integer idUsuario, Integer idProducto) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");
        Integer idProductoSeguro = Objects.requireNonNull(idProducto, "El id del producto no puede ser null");

        return favoritoRepository.findByUsuarioIdUsuarioAndProductoIdProducto(idUsuarioSeguro, idProductoSeguro)
                .isPresent();
    }
}