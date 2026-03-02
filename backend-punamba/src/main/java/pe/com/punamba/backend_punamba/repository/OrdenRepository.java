package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Orden;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Integer> {
    List<Orden> findByUsuarioIdUsuarioOrderByFechaOrdenDesc(Integer idUsuario);
    Optional<Orden> findByNumeroOrden(String numeroOrden);
}