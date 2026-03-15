package pe.com.punamba.backend_punamba.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.punamba.backend_punamba.entity.Atributo;
import pe.com.punamba.backend_punamba.repository.AtributoRepository;

@Service
public class AtributoService {

    private final AtributoRepository atributoRepository;

    public AtributoService(AtributoRepository atributoRepository) {
        this.atributoRepository = atributoRepository;
    }

    @Transactional(readOnly = true)
    public List<Atributo> listarTodos() {
        return atributoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Atributo buscarPorId(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id del atributo no puede ser null");

        return atributoRepository.findById(idSeguro)
                .orElseThrow(() -> new RuntimeException("Atributo no encontrado"));
    }

    @Transactional
    public Atributo crear(String nombre, String tipoDato, String unidad) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("El nombre del atributo es obligatorio");
        }

        if (atributoRepository.existsByNombreIgnoreCase(nombre.trim())) {
            throw new RuntimeException("El atributo ya existe");
        }

        Atributo atributo = new Atributo();
        atributo.setNombre(nombre.trim());
        atributo.setTipoDato((tipoDato == null || tipoDato.isBlank()) ? "select" : tipoDato.trim());
        atributo.setUnidad(unidad != null && !unidad.isBlank() ? unidad.trim() : null);

        return atributoRepository.save(atributo);
    }

    @Transactional
    public Atributo editar(Integer id, String nombre, String tipoDato, String unidad) {
        Integer idSeguro = Objects.requireNonNull(id, "El id del atributo no puede ser null");
        Atributo atributo = buscarPorId(idSeguro);

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("El nombre del atributo es obligatorio");
        }

        if (!atributo.getNombre().equalsIgnoreCase(nombre.trim())
                && atributoRepository.existsByNombreIgnoreCase(nombre.trim())) {
            throw new RuntimeException("Ya existe otro atributo con ese nombre");
        }

        atributo.setNombre(nombre.trim());
        atributo.setTipoDato((tipoDato == null || tipoDato.isBlank()) ? atributo.getTipoDato() : tipoDato.trim());
        atributo.setUnidad(unidad != null && !unidad.isBlank() ? unidad.trim() : null);

        return atributoRepository.save(atributo);
    }
}