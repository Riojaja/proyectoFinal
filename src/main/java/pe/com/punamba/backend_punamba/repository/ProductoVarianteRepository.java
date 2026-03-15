package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.ProductoVariante;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Integer> {
    List<ProductoVariante> findByProductoIdProducto(Integer idProducto);
    Optional<ProductoVariante> findBySku(String sku);
}