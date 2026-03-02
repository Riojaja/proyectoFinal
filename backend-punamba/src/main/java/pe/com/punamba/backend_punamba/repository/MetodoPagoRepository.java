package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.MetodoPago;
import java.util.List;
import java.util.Optional;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {
    
    List<MetodoPago> findByActivoTrue();
    
    Optional<MetodoPago> findByNombreIgnoreCase(String nombre); 
}