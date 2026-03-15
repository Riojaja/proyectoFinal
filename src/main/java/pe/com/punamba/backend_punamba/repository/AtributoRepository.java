package pe.com.punamba.backend_punamba.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.com.punamba.backend_punamba.entity.Atributo;

public interface AtributoRepository extends JpaRepository<Atributo, Integer> {
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Atributo> findByNombreIgnoreCase(String nombre);
}
