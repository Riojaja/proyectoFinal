package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.punamba.backend_punamba.entity.IconoCategoria;

import java.util.List;
import java.util.Optional;

public interface IconoCategoriaRepository extends JpaRepository<IconoCategoria, Integer> {

    List<IconoCategoria> findByEstadoTrueOrderByNombreAsc();

    List<IconoCategoria> findAllByOrderByNombreAsc();

    Optional<IconoCategoria> findByClaseCss(String claseCss);

    boolean existsByClaseCss(String claseCss);
}