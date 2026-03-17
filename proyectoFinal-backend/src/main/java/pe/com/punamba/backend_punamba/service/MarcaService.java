package pe.com.punamba.backend_punamba.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Marca;
import pe.com.punamba.backend_punamba.repository.MarcaRepository;

import java.util.List;
import java.util.Objects;

@Service
public class MarcaService {

    private final MarcaRepository marcaRepository;

    public MarcaService(MarcaRepository marcaRepository) {
        this.marcaRepository = marcaRepository;
    }

    @Transactional(readOnly = true)
    public List<Marca> listarTodas() {
        return marcaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Marca> listarActivas() {
        return marcaRepository.findByEstadoTrue();
    }

    @Transactional(readOnly = true)
    public Marca buscarPorId(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id de la marca no puede ser null");

        return marcaRepository.findById(idSeguro)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada"));
    }

    @Transactional
    public Marca crear(String nombre, Boolean estado) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("El nombre de la marca es obligatorio");
        }

        if (marcaRepository.existsByNombreIgnoreCase(nombre.trim())) {
            throw new RuntimeException("La marca ya existe");
        }

        boolean estadoFinal = (estado != null) ? estado : true;

        Marca marca = new Marca();
        marca.setNombre(nombre.trim());
        marca.setEstado(estadoFinal);

        return marcaRepository.save(marca);
    }

    @Transactional
    public Marca editar(Integer id, String nombre, Boolean estado) {
        Integer idSeguro = Objects.requireNonNull(id, "El id de la marca no puede ser null");
        Marca marca = buscarPorId(idSeguro);

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("El nombre de la marca es obligatorio");
        }

        if (!marca.getNombre().equalsIgnoreCase(nombre.trim())
                && marcaRepository.existsByNombreIgnoreCase(nombre.trim())) {
            throw new RuntimeException("Ya existe otra marca con ese nombre");
        }

        Boolean estadoActual = marca.getEstado();
        boolean estadoFinal = (estado != null) ? estado : (estadoActual != null ? estadoActual : true);

        marca.setNombre(nombre.trim());
        marca.setEstado(estadoFinal);

        return marcaRepository.save(marca);
    }
}