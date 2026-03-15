package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Transaccion;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Integer> {
    
    List<Transaccion> findByOrdenIdOrden(Integer idOrden);
    
    Optional<Transaccion> findByNumeroTransaccion(String numeroTransaccion);
}