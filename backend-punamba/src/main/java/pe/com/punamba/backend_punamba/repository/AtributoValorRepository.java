package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.AtributoValor;
import java.util.List;

@Repository
public interface AtributoValorRepository extends JpaRepository<AtributoValor, Integer> {
    List<AtributoValor> findByAtributoIdAtributo(Integer idAtributo);
}