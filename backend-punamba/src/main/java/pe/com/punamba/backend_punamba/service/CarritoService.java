package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.*;
import pe.com.punamba.backend_punamba.repository.*;

import java.util.Optional;

@Service
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoVarianteRepository varianteRepository;

    @Transactional
    public Carrito obtenerPorUsuario(Integer idUsuario) {
        return carritoRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(idUsuario)
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + idUsuario));
                    
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setUsuario(usuario);
                    return carritoRepository.save(nuevoCarrito);
                });
    }

    @Transactional
    public Carrito agregarProducto(Integer idUsuario, Integer idVariante, Integer cantidad) {
        if (cantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }

        Carrito carrito = obtenerPorUsuario(idUsuario);
        ProductoVariante variante = varianteRepository.findById(idVariante)
                .orElseThrow(() -> new RuntimeException("Variante de producto no encontrada"));
        
        Optional<CarritoItem> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getVariante().getIdVariante().equals(idVariante))
                .findFirst();

        if (itemExistente.isPresent()) {
            CarritoItem item = itemExistente.get();
            item.setCantidad(item.getCantidad() + cantidad);
        } else {
            CarritoItem nuevoItem = new CarritoItem();
            nuevoItem.setCarrito(carrito);
            nuevoItem.setVariante(variante);
            nuevoItem.setCantidad(cantidad);
            nuevoItem.setPrecioSnapshot(variante.getPrecio()); 
            
            carrito.getItems().add(nuevoItem);
        }

        return carritoRepository.save(carrito);
    }

    @Transactional
    public void eliminarItem(Integer idUsuario, Integer idVariante) {
        Carrito carrito = obtenerPorUsuario(idUsuario);
        carrito.getItems().removeIf(item -> item.getVariante().getIdVariante().equals(idVariante));
        carritoRepository.save(carrito);
    }

    @Transactional
    public void limpiarCarrito(Integer idUsuario) {
        Carrito carrito = obtenerPorUsuario(idUsuario);
        carrito.getItems().clear();
        carritoRepository.save(carrito);
    }
}