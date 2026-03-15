package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.EstadoOrden;

import java.util.Optional;

@Repository
public interface EstadoOrdenRepository extends JpaRepository<EstadoOrden, Integer> {
    Optional<EstadoOrden> findByNombre(String nombre);
}