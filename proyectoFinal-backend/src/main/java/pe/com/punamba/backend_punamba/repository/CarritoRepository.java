package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Carrito;
import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Integer> {
    Optional<Carrito> findByUsuarioIdUsuario(Integer idUsuario);
}