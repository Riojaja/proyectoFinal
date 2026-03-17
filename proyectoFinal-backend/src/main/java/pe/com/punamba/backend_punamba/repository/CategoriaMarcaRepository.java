package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.punamba.backend_punamba.entity.CategoriaMarca;
import pe.com.punamba.backend_punamba.entity.CategoriaMarcaId;

import java.util.List;
import java.util.Optional;

public interface CategoriaMarcaRepository extends JpaRepository<CategoriaMarca, CategoriaMarcaId> {

    List<CategoriaMarca> findByCategoriaIdCategoria(Integer idCategoria);

    List<CategoriaMarca> findByMarcaIdMarca(Integer idMarca);

    boolean existsByCategoriaIdCategoriaAndMarcaIdMarca(Integer idCategoria, Integer idMarca);

    Optional<CategoriaMarca> findByCategoriaIdCategoriaAndMarcaIdMarca(Integer idCategoria, Integer idMarca);
}