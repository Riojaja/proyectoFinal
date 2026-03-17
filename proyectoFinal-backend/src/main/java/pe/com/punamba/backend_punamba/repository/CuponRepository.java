package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Cupon;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuponRepository extends JpaRepository<Cupon, Integer> {

    Optional<Cupon> findByCodigoAndActivoTrue(String codigo);

    List<Cupon> findByVendedorIdVendedor(Integer idVendedor);

    boolean existsByCodigoIgnoreCase(String codigo);
}