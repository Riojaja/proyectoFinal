package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Valoracion;
import java.util.List;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Integer> {
    
    List<Valoracion> findByVarianteProductoIdProductoOrderByFechaCreacionDesc(Integer idProducto);
    
    List<Valoracion> findByUsuarioIdUsuario(Integer idUsuario);
}