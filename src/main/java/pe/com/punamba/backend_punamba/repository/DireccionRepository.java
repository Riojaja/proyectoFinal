package pe.com.punamba.backend_punamba.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.com.punamba.backend_punamba.entity.Direccion;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Integer> {

    List<Direccion> findByUsuarioIdUsuarioOrderByEsPrincipalDescIdDireccionDesc(Integer idUsuario);
}