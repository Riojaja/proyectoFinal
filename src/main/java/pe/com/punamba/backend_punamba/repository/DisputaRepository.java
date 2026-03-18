package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Disputa;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisputaRepository extends JpaRepository<Disputa, Integer> {

    List<Disputa> findAllByOrderByFechaAperturaDesc();

    List<Disputa> findByUsuarioIdUsuarioOrderByFechaAperturaDesc(Integer idUsuario);

    List<Disputa> findByOrdenDetalleVendedorUsuarioIdUsuarioOrderByFechaAperturaDesc(Integer idUsuario);

    Optional<Disputa> findByOrdenDetalleIdOrdenDetalle(Integer idOrdenDetalle);

    List<Disputa> findByEstadoOrderByFechaAperturaDesc(Disputa.EstadoDisputa estado);
}