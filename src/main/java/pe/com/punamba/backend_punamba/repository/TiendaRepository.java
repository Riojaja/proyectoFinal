package pe.com.punamba.backend_punamba.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.com.punamba.backend_punamba.entity.Tienda;

@Repository
public interface TiendaRepository extends JpaRepository<Tienda, Integer> {

    Optional<Tienda> findByVendedorIdVendedor(Integer idVendedor);

    List<Tienda> findByEstadoTrue();

    boolean existsByNombre(String nombre);
}