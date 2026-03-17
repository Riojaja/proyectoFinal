package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Orden;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Integer> {

    List<Orden> findByUsuarioIdUsuarioOrderByFechaOrdenDesc(Integer idUsuario);

    Optional<Orden> findByNumeroOrden(String numeroOrden);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Orden o")
    BigDecimal obtenerGmvTotal();

    @Query("""
        SELECT FUNCTION('MONTH', o.fechaOrden), COALESCE(SUM(o.total), 0)
        FROM Orden o
        WHERE FUNCTION('YEAR', o.fechaOrden) = :anio
        GROUP BY FUNCTION('MONTH', o.fechaOrden)
        ORDER BY FUNCTION('MONTH', o.fechaOrden)
    """)
    List<Object[]> obtenerVentasMensuales(@Param("anio") int anio);

    @Query(value = """
        SELECT WEEK(o.fecha_orden, 1) AS semana, COALESCE(SUM(o.total), 0) AS total
        FROM ordenes o
        WHERE YEAR(o.fecha_orden) = :anio
        GROUP BY WEEK(o.fecha_orden, 1)
        ORDER BY WEEK(o.fecha_orden, 1)
    """, nativeQuery = true)
    List<Object[]> obtenerVentasSemanales(@Param("anio") int anio);
}