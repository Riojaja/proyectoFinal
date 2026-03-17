package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Ubigeo;

import java.util.List;

@Repository
public interface UbigeoRepository extends JpaRepository<Ubigeo, String> {

    @Query("SELECT DISTINCT u.departamento FROM Ubigeo u ORDER BY u.departamento ASC")
    List<String> findDistinctDepartamentos();

    @Query("SELECT DISTINCT u.provincia FROM Ubigeo u WHERE u.departamento = :departamento ORDER BY u.provincia ASC")
    List<String> findDistinctProvinciasByDepartamento(@Param("departamento") String departamento);

    @Query("SELECT u FROM Ubigeo u WHERE u.departamento = :departamento AND u.provincia = :provincia ORDER BY u.distrito ASC")
    List<Ubigeo> findDistritosByDepartamentoAndProvincia(@Param("departamento") String departamento, @Param("provincia") String provincia);
}