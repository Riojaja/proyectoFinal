package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.DisputaMensaje;

import java.util.List;

@Repository
public interface DisputaMensajeRepository extends JpaRepository<DisputaMensaje, Integer> {
    List<DisputaMensaje> findByDisputaIdDisputaOrderByFechaEnvioAsc(Integer idDisputa);
}