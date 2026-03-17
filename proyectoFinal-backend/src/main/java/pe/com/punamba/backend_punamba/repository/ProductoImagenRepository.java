package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.ProductoImagen;
import java.util.List;

@Repository
public interface ProductoImagenRepository extends JpaRepository<ProductoImagen, Integer> {
    List<ProductoImagen> findByProductoIdProductoOrderByOrdenAsc(Integer idProducto);
}