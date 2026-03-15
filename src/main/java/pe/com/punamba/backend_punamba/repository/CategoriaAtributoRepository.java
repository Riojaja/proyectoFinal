package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.punamba.backend_punamba.entity.CategoriaAtributo;
import pe.com.punamba.backend_punamba.entity.CategoriaAtributoId;

import java.util.List;
import java.util.Optional;

public interface CategoriaAtributoRepository extends JpaRepository<CategoriaAtributo, CategoriaAtributoId> {

    List<CategoriaAtributo> findByCategoriaIdCategoriaOrderByOrdenAsc(Integer idCategoria);

    boolean existsByCategoriaIdCategoriaAndAtributoIdAtributo(Integer idCategoria, Integer idAtributo);

    Optional<CategoriaAtributo> findByCategoriaIdCategoriaAndAtributoIdAtributo(Integer idCategoria,
            Integer idAtributo);
}