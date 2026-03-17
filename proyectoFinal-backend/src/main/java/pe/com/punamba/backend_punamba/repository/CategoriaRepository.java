package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Categoria;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    List<Categoria> findByCategoriaPadreIsNullAndEstadoTrue();

    List<Categoria> findByCategoriaPadreIdCategoria(Integer idPadre);

    boolean existsByNombre(String nombre);

    long countByEstadoTrue();
}
