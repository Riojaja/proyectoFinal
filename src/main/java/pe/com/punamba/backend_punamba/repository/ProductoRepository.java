package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Producto;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByCategoriaIdCategoriaAndEstado(Integer idCategoria, Producto.EstadoProducto estado);

    List<Producto> findByVendedorIdVendedor(Integer idVendedor);

    List<Producto> findByNombreContainingIgnoreCaseAndEstado(String nombre, Producto.EstadoProducto estado);

    List<Producto> findByEstado(Producto.EstadoProducto estado);

    List<Producto> findByEstado(Producto.EstadoProducto estado, Pageable pageable);

    List<Producto> findByEstadoOrderByFechaCreacionDesc(Producto.EstadoProducto estado, Pageable pageable);

    List<Producto> findByCategoriaIdCategoriaAndEstadoOrderByFechaCreacionDesc(
            Integer idCategoria,
            Producto.EstadoProducto estado,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT p
            FROM Producto p
            JOIN p.variantes v
            WHERE p.estado = :estado
              AND v.activo = true
              AND v.precioOferta IS NOT NULL
              AND v.precioOferta > 0
              AND v.precioOferta < v.precio
            ORDER BY p.fechaCreacion DESC
            """)
    List<Producto> findProductosEnOferta(Producto.EstadoProducto estado, Pageable pageable);

    @Query(value = """
            SELECT p.*
            FROM productos p
            JOIN variantes_producto vp ON vp.id_producto = p.id_producto
            JOIN ordenes_detalle od ON od.id_variante = vp.id_variante
            WHERE p.estado = 'activo'
            GROUP BY p.id_producto
            ORDER BY SUM(od.cantidad) DESC, MAX(p.fecha_creacion) DESC
            """, nativeQuery = true)
    List<Producto> findMasVendidos(Pageable pageable);
}
