package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.MetodoEnvio;
import pe.com.punamba.backend_punamba.repository.MetodoEnvioRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MetodoEnvioService {

    @Autowired
    private MetodoEnvioRepository metodoEnvioRepository;

    @Transactional(readOnly = true)
    public List<MetodoEnvio> listarActivos() {
        return metodoEnvioRepository.findByEstadoTrue();
    }

    @Transactional(readOnly = true)
    public List<MetodoEnvio> listarTodos() {
        return metodoEnvioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<MetodoEnvio> buscarPorId(Integer id) {
        return metodoEnvioRepository.findById(id);
    }

    @Transactional
    public MetodoEnvio guardar(MetodoEnvio metodoEnvio) {
        return metodoEnvioRepository.save(metodoEnvio);
    }

    @Transactional
    public void eliminar(Integer id) {
        metodoEnvioRepository.findById(id).ifPresent(m -> {
            m.setEstado(false);
            metodoEnvioRepository.save(m);
        });
    }
}