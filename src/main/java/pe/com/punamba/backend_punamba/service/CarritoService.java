package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Carrito;
import pe.com.punamba.backend_punamba.entity.CarritoItem;
import pe.com.punamba.backend_punamba.entity.ProductoVariante;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.repository.CarritoRepository;
import pe.com.punamba.backend_punamba.repository.ProductoVarianteRepository;
import pe.com.punamba.backend_punamba.repository.UsuarioRepository;

import java.util.Objects;
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
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");

        return carritoRepository.findByUsuarioIdUsuario(idUsuarioSeguro)
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(idUsuarioSeguro)
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + idUsuarioSeguro));

                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setUsuario(usuario);
                    return carritoRepository.save(nuevoCarrito);
                });
    }

    @Transactional
    public Carrito agregarProducto(Integer idUsuario, Integer idVariante, Integer cantidad) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");
        Integer idVarianteSeguro = Objects.requireNonNull(idVariante, "El id de la variante no puede ser null");

        if (cantidad == null || cantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }

        Carrito carrito = obtenerPorUsuario(idUsuarioSeguro);

        ProductoVariante variante = varianteRepository.findById(idVarianteSeguro)
                .orElseThrow(() -> new RuntimeException("Variante de producto no encontrada"));

        Integer stockDisponible = variante.getStock() != null ? variante.getStock() : 0;
        if (stockDisponible <= 0) {
            throw new RuntimeException("El producto no tiene stock disponible");
        }

        Optional<CarritoItem> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getVariante().getIdVariante().equals(idVarianteSeguro))
                .findFirst();

        if (itemExistente.isPresent()) {
            CarritoItem item = itemExistente.get();
            int nuevaCantidad = item.getCantidad() + cantidad;

            if (nuevaCantidad > stockDisponible) {
                throw new RuntimeException("Stock insuficiente para la cantidad solicitada");
            }

            item.setCantidad(nuevaCantidad);
        } else {
            if (cantidad > stockDisponible) {
                throw new RuntimeException("Stock insuficiente para la cantidad solicitada");
            }

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
    public Carrito actualizarCantidad(Integer idUsuario, Integer idVariante, Integer cantidad) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");
        Integer idVarianteSeguro = Objects.requireNonNull(idVariante, "El id de la variante no puede ser null");

        if (cantidad == null || cantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }

        Carrito carrito = obtenerPorUsuario(idUsuarioSeguro);

        CarritoItem item = carrito.getItems().stream()
                .filter(i -> i.getVariante().getIdVariante().equals(idVarianteSeguro))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("El producto no existe en el carrito"));

        Integer stockDisponible = item.getVariante().getStock() != null ? item.getVariante().getStock() : 0;
        if (cantidad > stockDisponible) {
            throw new RuntimeException("Stock insuficiente para la cantidad solicitada");
        }

        item.setCantidad(cantidad);

        return carritoRepository.save(carrito);
    }

    @Transactional
    public void eliminarItem(Integer idUsuario, Integer idVariante) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");
        Integer idVarianteSeguro = Objects.requireNonNull(idVariante, "El id de la variante no puede ser null");

        Carrito carrito = obtenerPorUsuario(idUsuarioSeguro);
        carrito.getItems().removeIf(item -> item.getVariante().getIdVariante().equals(idVarianteSeguro));
        carritoRepository.save(carrito);
    }

    @Transactional
    public void limpiarCarrito(Integer idUsuario) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El id del usuario no puede ser null");

        Carrito carrito = obtenerPorUsuario(idUsuarioSeguro);
        carrito.getItems().clear();
        carritoRepository.save(carrito);
    }
}