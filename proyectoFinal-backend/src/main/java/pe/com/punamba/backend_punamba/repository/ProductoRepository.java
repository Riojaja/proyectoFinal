package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Producto;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // ============================================
    // BÚSQUEDAS POR CATEGORÍA
    // ============================================
    List<Producto> findByCategoriaIdCategoriaAndEstado(Integer idCategoria, Producto.EstadoProducto estado);
    
    List<Producto> findByCategoriaIdCategoriaAndEstadoOrderByFechaCreacionDesc(
            Integer idCategoria,
            Producto.EstadoProducto estado,
            Pageable pageable
    );

    // ============================================
    // BÚSQUEDAS POR VENDEDOR
    // ============================================
    List<Producto> findByVendedorIdVendedor(Integer idVendedor);

    // ============================================
    // BÚSQUEDAS POR NOMBRE/DESCRIPCIÓN
    // ============================================
    List<Producto> findByNombreContainingIgnoreCaseAndEstado(String nombre, Producto.EstadoProducto estado);
    
    List<Producto> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
            String nombre, String descripcion, Pageable pageable);

    // ============================================
    // BÚSQUEDAS POR ESTADO
    // ============================================
    List<Producto> findByEstado(Producto.EstadoProducto estado);
    
    List<Producto> findByEstado(Producto.EstadoProducto estado, Pageable pageable);
    
    List<Producto> findByEstadoOrderByFechaCreacionDesc(Producto.EstadoProducto estado, Pageable pageable);

    // ============================================
    // BÚSQUEDA UNIVERSAL (PARA EL BUSCADOR)
    // ============================================
    @Query("SELECT DISTINCT p FROM Producto p " +
    	       "LEFT JOIN p.categoria c " +
    	       "LEFT JOIN p.variantes v " +
    	       "LEFT JOIN v.atributos va " +
    	       "LEFT JOIN va.atributo a " +
    	       "LEFT JOIN va.valor av " +
    	       "WHERE p.estado = 'activo' AND (" +
    	       "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
    	       "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
    	       "LOWER(p.modelo) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
    	       "LOWER(p.marca) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +  // 👈 'marca' es String, no entidad
    	       "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
    	       "LOWER(v.sku) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
    	       "LOWER(a.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
    	       "LOWER(av.valor) LIKE LOWER(CONCAT('%', :termino, '%')))")
    	List<Producto> busquedaUniversal(@Param("termino") String termino, Pageable pageable);
    // ============================================
    // PRODUCTOS EN OFERTA
    // ============================================
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
    List<Producto> findProductosEnOferta(@Param("estado") Producto.EstadoProducto estado, Pageable pageable);

    // ============================================
    // PRODUCTOS MÁS VENDIDOS (consulta nativa)
    // ============================================
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