package pe.com.punamba.backend_punamba.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.com.punamba.backend_punamba.entity.MetodoEnvio;

@Repository
public interface MetodoEnvioRepository extends JpaRepository<MetodoEnvio, Integer> {
    List<MetodoEnvio> findByEstadoTrue();
}