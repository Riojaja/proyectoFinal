package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Producto;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    
    List<Producto> findByCategoriaIdCategoriaAndEstado(Integer idCategoria, Producto.EstadoProducto estado);
    
    List<Producto> findByVendedorIdVendedor(Integer idVendedor);
    
    List<Producto> findByNombreContainingIgnoreCaseAndEstado(String nombre, Producto.EstadoProducto estado);

    List<Producto> findByEstado(Producto.EstadoProducto estado);
}