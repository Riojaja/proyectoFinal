package pe.com.punamba.backend_punamba.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Atributo;
import pe.com.punamba.backend_punamba.entity.AtributoValor;
import pe.com.punamba.backend_punamba.entity.Producto;
import pe.com.punamba.backend_punamba.entity.ProductoImagen;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.entity.Vendedor;
import pe.com.punamba.backend_punamba.repository.ProductoImagenRepository;
import pe.com.punamba.backend_punamba.repository.ProductoRepository;
import pe.com.punamba.backend_punamba.repository.VendedorRepository;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
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
    public List<Producto> listarActivos(Integer limit) {
        return productoRepository.findByEstado(
                Producto.EstadoProducto.activo,
                PageRequest.of(0, normalizarLimit(limit))
        );
    }

    @Transactional(readOnly = true)
    public List<Producto> listarPorVendedor(Integer idVendedor) {
        Integer idVendedorSeguro = Objects.requireNonNull(idVendedor, "El id del vendedor no puede ser null");
        return productoRepository.findByVendedorIdVendedor(idVendedorSeguro);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id del producto no puede ser null");
        return productoRepository.findById(idSeguro);
    }

    @Transactional(readOnly = true)
    public List<ProductoImagen> listarImagenesPorProducto(Integer idProducto) {
        Integer idProductoSeguro = Objects.requireNonNull(idProducto, "El id del producto no puede ser null");
        return productoImagenRepository.findByProductoIdProductoOrderByOrdenAsc(idProductoSeguro);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarHome(Integer idCategoria, String tipo, Integer limit) {
        int size = normalizarLimit(limit);
        List<Producto> base;

        String tipoNormalizado = tipo == null ? "" : tipo.trim().toLowerCase(Locale.ROOT);

        switch (tipoNormalizado) {
            case "ofertas" -> base = productoRepository.findProductosEnOferta(
                    Producto.EstadoProducto.activo,
                    PageRequest.of(0, size)
            );

            case "nuevos" -> base = productoRepository.findByEstadoOrderByFechaCreacionDesc(
                    Producto.EstadoProducto.activo,
                    PageRequest.of(0, size)
            );

            case "mas-vendidos" -> {
                base = productoRepository.findMasVendidos(PageRequest.of(0, size));
                if (base == null || base.isEmpty()) {
                    base = productoRepository.findByEstadoOrderByFechaCreacionDesc(
                            Producto.EstadoProducto.activo,
                            PageRequest.of(0, size)
                    );
                }
            }

            default -> base = productoRepository.findByEstado(
                    Producto.EstadoProducto.activo,
                    PageRequest.of(0, size)
            );
        }

        if (idCategoria != null) {
            return base.stream()
                    .filter(p -> p.getCategoria() != null
                            && p.getCategoria().getIdCategoria() != null
                            && p.getCategoria().getIdCategoria().equals(idCategoria))
                    .toList();
        }

        return base;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarOfertas(Integer limit) {
        return productoRepository.findProductosEnOferta(
                Producto.EstadoProducto.activo,
                PageRequest.of(0, normalizarLimit(limit))
        );
    }

    @Transactional(readOnly = true)
    public List<Producto> listarNuevos(Integer limit) {
        return productoRepository.findByEstadoOrderByFechaCreacionDesc(
                Producto.EstadoProducto.activo,
                PageRequest.of(0, normalizarLimit(limit))
        );
    }

    @Transactional(readOnly = true)
    public List<Producto> listarMasVendidos(Integer limit) {
        List<Producto> lista = productoRepository.findMasVendidos(
                PageRequest.of(0, normalizarLimit(limit))
        );

        if (lista == null || lista.isEmpty()) {
            return listarNuevos(limit);
        }

        return lista;
    }

    // ✅ BÚSQUEDA UNIVERSAL
    @Transactional(readOnly = true)
    public List<Producto> buscarPorTermino(String termino, int limit) {
        if (termino == null || termino.trim().isEmpty()) {
            return List.of();
        }
        
        String terminoLower = "%" + termino.toLowerCase() + "%";
        return productoRepository.busquedaUniversal(terminoLower, PageRequest.of(0, limit));
    }

    @Transactional
    public Producto guardarConRelaciones(Producto producto, Vendedor vendedor) {
        Producto productoSeguro = Objects.requireNonNull(producto, "El producto no puede ser null");
        Vendedor vendedorSeguro = Objects.requireNonNull(vendedor, "El vendedor no puede ser null");

        productoSeguro.setVendedor(vendedorSeguro);
        vincularHijos(productoSeguro);
        return productoRepository.save(productoSeguro);
    }

    @Transactional
    public Producto actualizar(Integer id, Producto detalles, Usuario usuario) {
        Integer idSeguro = Objects.requireNonNull(id, "El id del producto no puede ser null");
        Producto detallesSeguro = Objects.requireNonNull(detalles, "Los detalles del producto no pueden ser null");
        Usuario usuarioSeguro = Objects.requireNonNull(usuario, "El usuario no puede ser null");

        validarPropiedad(idSeguro, usuarioSeguro);

        Producto productoExistente = productoRepository.findById(idSeguro)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        productoExistente.setNombre(detallesSeguro.getNombre());
        productoExistente.setDescripcion(detallesSeguro.getDescripcion());
        productoExistente.setMarca(detallesSeguro.getMarca());
        productoExistente.setModelo(detallesSeguro.getModelo());
        productoExistente.setPrecioBase(detallesSeguro.getPrecioBase());
        productoExistente.setCategoria(detallesSeguro.getCategoria());

        if (detallesSeguro.getVariantes() != null) {
            productoExistente.getVariantes().clear();
            productoExistente.getVariantes().addAll(detallesSeguro.getVariantes());
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
                            AtributoValor valorRef = entityManager.getReference(
                                    AtributoValor.class,
                                    va.getValor().getIdValor()
                            );
                            va.setValor(valorRef);
                        }
                    });
                }
            });
        }
    }

    @Transactional
    public ProductoImagen agregarImagenUrl(Integer idProducto, String urlImagen, Usuario usuario) {
        Integer idProductoSeguro = Objects.requireNonNull(idProducto, "El id del producto no puede ser null");
        String urlImagenSegura = Objects.requireNonNull(urlImagen, "La URL de la imagen no puede ser null");
        Usuario usuarioSeguro = Objects.requireNonNull(usuario, "El usuario no puede ser null");

        validarPropiedad(idProductoSeguro, usuarioSeguro);

        Producto producto = productoRepository.findById(idProductoSeguro)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ProductoImagen nuevaImagen = new ProductoImagen();
        nuevaImagen.setProducto(producto);
        nuevaImagen.setUrlImagen(urlImagenSegura);

        boolean tienePrincipal = producto.getImagenes().stream()
                .anyMatch(img -> Boolean.TRUE.equals(img.getEsPrincipal()));

        nuevaImagen.setEsPrincipal(!tienePrincipal);
        nuevaImagen.setOrden(producto.getImagenes().size() + 1);

        return productoImagenRepository.save(nuevaImagen);
    }

    public void validarPropiedad(Integer productoId, Usuario usuario) {
        Integer productoIdSeguro = Objects.requireNonNull(productoId, "El id del producto no puede ser null");
        Usuario usuarioSeguro = Objects.requireNonNull(usuario, "El usuario no puede ser null");

        Producto producto = productoRepository.findById(productoIdSeguro)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Vendedor vendedor = vendedorRepository.findByUsuarioIdUsuario(usuarioSeguro.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("El usuario no tiene un perfil de vendedor activo"));

        if (!producto.getVendedor().getIdVendedor().equals(vendedor.getIdVendedor())) {
            throw new RuntimeException("No tienes permiso para modificar este producto");
        }
    }

    @Transactional
    public void eliminarSeguro(Integer id, Usuario usuario) {
        Integer idSeguro = Objects.requireNonNull(id, "El id del producto no puede ser null");
        Usuario usuarioSeguro = Objects.requireNonNull(usuario, "El usuario no puede ser null");

        validarPropiedad(idSeguro, usuarioSeguro);

        Optional<Producto> productoOpt = productoRepository.findById(idSeguro);
        if (productoOpt.isPresent()) {
            Producto producto = Objects.requireNonNull(productoOpt.get(), "El producto no puede ser null");
            producto.setEstado(Producto.EstadoProducto.inactivo);
            productoRepository.save(producto);
        }
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCaseAndEstado(
                nombre,
                Producto.EstadoProducto.activo
        );
    }

    @Transactional(readOnly = true)
    public List<Producto> listarPorCategoria(Integer idCategoria) {
        Integer idCategoriaSeguro = Objects.requireNonNull(idCategoria, "El id de la categoría no puede ser null");

        return productoRepository.findByCategoriaIdCategoriaAndEstado(
                idCategoriaSeguro,
                Producto.EstadoProducto.activo
        );
    }

    @Transactional(readOnly = true)
    public List<Producto> listarPorCategoria(Integer idCategoria, int limit) {
        Integer idCategoriaSeguro = Objects.requireNonNull(idCategoria, "El id de la categoría no puede ser null");

        return productoRepository.findByCategoriaIdCategoriaAndEstadoOrderByFechaCreacionDesc(
                idCategoriaSeguro,
                Producto.EstadoProducto.activo,
                PageRequest.of(0, limit)
        );
    }

    private int normalizarLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 12;
        }
        return Math.min(limit, 50);
    }
}