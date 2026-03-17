package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Valoracion;
import pe.com.punamba.backend_punamba.repository.ValoracionRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ValoracionService {

    @Autowired
    private ValoracionRepository valoracionRepository;

    @Transactional(readOnly = true)
    public List<Valoracion> listarPorProducto(Integer idProducto) {
        Integer idProductoSeguro = Objects.requireNonNull(idProducto, "El idProducto no puede ser null");
        return valoracionRepository.findByVarianteProductoIdProductoOrderByFechaCreacionDesc(idProductoSeguro);
    }

    @Transactional(readOnly = true)
    public List<Valoracion> listarPorUsuario(Integer idUsuario) {
        Integer idUsuarioSeguro = Objects.requireNonNull(idUsuario, "El idUsuario no puede ser null");
        return valoracionRepository.findByUsuarioIdUsuario(idUsuarioSeguro);
    }

    @Transactional(readOnly = true)
    public Optional<Valoracion> buscarPorId(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id no puede ser null");
        return valoracionRepository.findById(idSeguro);
    }

    @Transactional
    public Valoracion guardar(Valoracion valoracion) {
        Valoracion valoracionSegura = Objects.requireNonNull(valoracion, "La valoración no puede ser null");
        return valoracionRepository.save(valoracionSegura);
    }

    @Transactional
    public void eliminar(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id no puede ser null");
        valoracionRepository.deleteById(idSeguro);
    }
}