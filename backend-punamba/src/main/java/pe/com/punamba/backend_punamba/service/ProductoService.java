package pe.com.punamba.backend_punamba.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.*;
import pe.com.punamba.backend_punamba.repository.ProductoImagenRepository;
import pe.com.punamba.backend_punamba.repository.ProductoRepository;
import pe.com.punamba.backend_punamba.repository.VendedorRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private VendedorRepository vendedorRepository;

    @Autowired
    private ProductoImagenRepository productoImagenRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        return productoRepository.findByEstado(Producto.EstadoProducto.activo);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Integer id) {
        return productoRepository.findById(id);
    }

    @Transactional
    public Producto guardarConRelaciones(Producto producto, Vendedor vendedor) {
        producto.setVendedor(vendedor);
        vincularHijos(producto);
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(Integer id, Producto detalles, Usuario usuario) {
        validarPropiedad(id, usuario);
        
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        productoExistente.setNombre(detalles.getNombre());
        productoExistente.setDescripcion(detalles.getDescripcion());
        productoExistente.setMarca(detalles.getMarca());
        productoExistente.setModelo(detalles.getModelo());
        productoExistente.setPrecioBase(detalles.getPrecioBase());
        productoExistente.setCategoria(detalles.getCategoria());

        if (detalles.getImagenes() != null) {
            productoExistente.getImagenes().clear();
            productoExistente.getImagenes().addAll(detalles.getImagenes());
        }
        
        if (detalles.getVariantes() != null) {
            productoExistente.getVariantes().clear();
            productoExistente.getVariantes().addAll(detalles.getVariantes());
        }

        vincularHijos(productoExistente);
        return productoRepository.save(productoExistente);
    }

    private void vincularHijos(Producto p) {
        if (p.getImagenes() != null) {
            p.getImagenes().forEach(img -> img.setProducto(p));
        }

        if (p.getVariantes() != null) {
            p.getVariantes().forEach(v -> {
                v.setProducto(p);
                if (v.getAtributos() != null) {
                    v.getAtributos().forEach(va -> {
                        va.setVariante(v);
                        if (va.getAtributo() != null && va.getAtributo().getIdAtributo() != null) {
                            Atributo ref = entityManager.getReference(Atributo.class, va.getAtributo().getIdAtributo());
                            va.setAtributo(ref);
                        }
                        if (va.getValor() != null && va.getValor().getIdValor() != null) {
                            AtributoValor valorRef = entityManager.getReference(AtributoValor.class, va.getValor().getIdValor());
                            va.setValor(valorRef);
                        }
                    });
                }
            });
        }
    }

    @Transactional
    public ProductoImagen agregarImagenUrl(Integer idProducto, String urlImagen, Usuario usuario) {
        validarPropiedad(idProducto, usuario);

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ProductoImagen nuevaImagen = new ProductoImagen();
        nuevaImagen.setProducto(producto);
        nuevaImagen.setUrlImagen(urlImagen);
        
        boolean tienePrincipal = producto.getImagenes().stream().anyMatch(ProductoImagen::getEsPrincipal);
        nuevaImagen.setEsPrincipal(!tienePrincipal);
        nuevaImagen.setOrden(producto.getImagenes().size());

        return productoImagenRepository.save(nuevaImagen);
    }

    public void validarPropiedad(Integer productoId, Usuario usuario) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Vendedor vendedor = vendedorRepository.findByUsuarioIdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("El usuario no tiene un perfil de vendedor activo"));

        if (!producto.getVendedor().getIdVendedor().equals(vendedor.getIdVendedor())) {
            throw new RuntimeException("No tienes permiso para modificar este producto");
        }
    }

    @Transactional
    public void eliminarSeguro(Integer id, Usuario usuario) {
        validarPropiedad(id, usuario);
        productoRepository.findById(id).ifPresent(p -> {
            p.setEstado(Producto.EstadoProducto.inactivo);
            productoRepository.save(p);
        });
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCaseAndEstado(nombre, Producto.EstadoProducto.activo);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarPorCategoria(Integer idCategoria) {
        return productoRepository.findByCategoriaIdCategoriaAndEstado(idCategoria, Producto.EstadoProducto.activo);
    }
}