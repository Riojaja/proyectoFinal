package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Vendedor;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendedorRepository extends JpaRepository<Vendedor, Integer> {

    Optional<Vendedor> findByRuc(String ruc);

    Optional<Vendedor> findByNombreTienda(String nombreTienda);

    Optional<Vendedor> findByNombreTiendaIgnoreCase(String nombreTienda);

    Optional<Vendedor> findByUsuarioIdUsuario(Integer idUsuario);

    boolean existsByRuc(String ruc);

    boolean existsByNombreTienda(String nombreTienda);

    boolean existsByNombreTiendaIgnoreCase(String nombreTienda);

    boolean existsByUsuarioIdUsuario(Integer idUsuario);

    long countByEstado(Vendedor.EstadoVendedor estado);

    List<Vendedor> findTop5ByOrderByFechaRegistroDesc();
}