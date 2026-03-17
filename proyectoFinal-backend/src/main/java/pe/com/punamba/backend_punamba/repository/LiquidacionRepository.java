package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Liquidacion;
import java.util.List;

@Repository
public interface LiquidacionRepository extends JpaRepository<Liquidacion, Integer> {
    
    List<Liquidacion> findByVendedorIdVendedorOrderByFechaCorteDesc(Integer idVendedor);
    
    List<Liquidacion> findByEstado(Liquidacion.EstadoLiquidacion estado);
}