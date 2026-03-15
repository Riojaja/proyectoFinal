package pe.com.punamba.backend_punamba.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.com.punamba.backend_punamba.entity.Marca;

public interface MarcaRepository extends JpaRepository<Marca, Integer> {
    Optional<Marca> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
    List<Marca> findByEstadoTrue();
}