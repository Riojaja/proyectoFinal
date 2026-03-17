package pe.com.punamba.backend_punamba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.punamba.backend_punamba.entity.Favorito;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Integer> {
    
    List<Favorito> findByUsuarioIdUsuario(Integer idUsuario);

    Optional<Favorito> findByUsuarioIdUsuarioAndProductoIdProducto(Integer idUsuario, Integer idProducto);
}