package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Atributo;
import java.util.Optional;

@Repository
public interface AtributoRepository extends JpaRepository<Atributo, Integer> {
    Optional<Atributo> findByNombre(String nombre);
}