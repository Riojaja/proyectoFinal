package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.dto.CategoriaPromoDTO;
import pe.com.punamba.backend_punamba.entity.Categoria;
import pe.com.punamba.backend_punamba.entity.Producto;
import pe.com.punamba.backend_punamba.repository.CategoriaRepository;
import pe.com.punamba.backend_punamba.repository.ProductoRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<Categoria> listarPrincipales() {
        return categoriaRepository.findByCategoriaPadreIsNullAndEstadoTrue();
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Categoria> buscarPorId(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id de la categoría no puede ser null");
        return categoriaRepository.findById(idSeguro);
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarSubcategorias(Integer idPadre) {
        Integer idPadreSeguro = Objects.requireNonNull(idPadre, "El id de la categoría padre no puede ser null");
        return categoriaRepository.findByCategoriaPadreIdCategoria(idPadreSeguro);
    }

    @Transactional
    public Categoria guardar(Categoria categoria) {
        Categoria categoriaSegura = Objects.requireNonNull(categoria, "La categoría no puede ser null");
        return categoriaRepository.save(categoriaSegura);
    }

    @Transactional
    public void eliminar(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id de la categoría no puede ser null");

        Optional<Categoria> categoriaOpt = categoriaRepository.findById(idSeguro);
        if (categoriaOpt.isPresent()) {
            Categoria categoria = categoriaOpt.get();
            categoria.setEstado(false);
            categoriaRepository.save(categoria);
        }
    }

    @Transactional(readOnly = true)
    public boolean existeNombre(String nombre) {
        return categoriaRepository.existsByNombre(nombre);
    }

    @Transactional(readOnly = true)
    public List<CategoriaPromoDTO> listarPromosDestacadas(Integer limit) {
        int max = (limit == null || limit <= 0) ? 3 : Math.min(limit, 10);

        List<Categoria> categorias = categoriaRepository.findByCategoriaPadreIsNullAndEstadoTrue();
        List<CategoriaPromoDTO> salida = new ArrayList<>();

        for (Categoria c : categorias) {
            if (salida.size() >= max) {
                break;
            }

            List<Producto> productos = productoRepository.findByCategoriaIdCategoriaAndEstadoOrderByFechaCreacionDesc(
                    c.getIdCategoria(),
                    Producto.EstadoProducto.activo,
                    PageRequest.of(0, 1)
            );

            if (productos.isEmpty()) {
                continue;
            }

            Producto p = productos.get(0);

            String imagen = null;
            if (p.getImagenes() != null && !p.getImagenes().isEmpty()) {
                imagen = p.getImagenes().stream()
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparingInt(img -> {
                            Integer orden = img.getOrden();
                            return orden != null ? orden : 999;
                        }))
                        .map(img -> img.getUrlImagen())
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
            }

            String precio = "Desde S/. " + (p.getPrecioBase() != null ? p.getPrecioBase() : "0.00");

            salida.add(new CategoriaPromoDTO(
                    c.getIdCategoria(),
                    c.getNombre(),
                    imagen,
                    "Colección destacada",
                    precio
            ));
        }

        return salida;
    }
}
