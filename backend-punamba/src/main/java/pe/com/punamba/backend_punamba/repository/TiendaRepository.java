package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Tienda;

import java.util.List;

@Repository
public interface TiendaRepository extends JpaRepository<Tienda, Integer> {
    
    List<Tienda> findByVendedorIdVendedor(Integer idVendedor);
    
    List<Tienda> findByEstadoTrue();

    boolean existsByNombre(String nombre);
}