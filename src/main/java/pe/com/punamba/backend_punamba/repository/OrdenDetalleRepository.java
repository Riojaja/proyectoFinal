package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.OrdenDetalle;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrdenDetalleRepository extends JpaRepository<OrdenDetalle, Integer> {

    List<OrdenDetalle> findByVendedorIdVendedor(Integer idVendedor);

    List<OrdenDetalle> findByOrdenIdOrden(Integer idOrden);

    @Query("SELECT COALESCE(SUM(od.comisionPlataforma), 0) FROM OrdenDetalle od")
    BigDecimal obtenerComisionesTotales();
}