package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Valoracion;
import pe.com.punamba.backend_punamba.repository.ValoracionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ValoracionService {

    @Autowired
    private ValoracionRepository valoracionRepository;

    @Transactional(readOnly = true)
    public List<Valoracion> listarPorProducto(Integer idProducto) {
        return valoracionRepository.findByVarianteProductoIdProductoOrderByFechaCreacionDesc(idProducto);
    }

    @Transactional(readOnly = true)
    public List<Valoracion> listarPorUsuario(Integer idUsuario) {
        return valoracionRepository.findByUsuarioIdUsuario(idUsuario);
    }

    @Transactional(readOnly = true)
    public Optional<Valoracion> buscarPorId(Integer id) {
        return valoracionRepository.findById(id);
    }

    @Transactional
    public Valoracion guardar(Valoracion valoracion) {
        return valoracionRepository.save(valoracion);
    }

    @Transactional
    public void eliminar(Integer id) {
        valoracionRepository.deleteById(id);
    }
}