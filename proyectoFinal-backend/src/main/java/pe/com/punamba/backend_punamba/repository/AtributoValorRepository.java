package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.punamba.backend_punamba.entity.AtributoValor;

import java.util.List;

public interface AtributoValorRepository extends JpaRepository<AtributoValor, Integer> {
    List<AtributoValor> findByAtributoIdAtributo(Integer idAtributo);

    boolean existsByAtributoIdAtributoAndValorIgnoreCase(Integer idAtributo, String valor);
}
