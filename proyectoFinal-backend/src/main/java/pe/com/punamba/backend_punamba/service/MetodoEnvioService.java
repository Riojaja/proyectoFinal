package pe.com.punamba.backend_punamba.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.punamba.backend_punamba.entity.MetodoEnvio;
import pe.com.punamba.backend_punamba.repository.MetodoEnvioRepository;

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
        Integer idSeguro = Objects.requireNonNull(id, "El id del método de envío no puede ser null");
        return metodoEnvioRepository.findById(idSeguro);
    }

    @Transactional
    public MetodoEnvio guardar(MetodoEnvio metodoEnvio) {
        MetodoEnvio metodoEnvioSeguro = Objects.requireNonNull(metodoEnvio, "El método de envío no puede ser null");
        return metodoEnvioRepository.save(metodoEnvioSeguro);
    }

    @Transactional
    public void eliminar(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id del método de envío no puede ser null");

        Optional<MetodoEnvio> metodoOpt = metodoEnvioRepository.findById(idSeguro);
        if (metodoOpt.isPresent()) {
            MetodoEnvio metodo = Objects.requireNonNull(metodoOpt.get(), "El método de envío no puede ser null");
            metodo.setEstado(false);
            metodoEnvioRepository.save(metodo);
        }
    }
}