package pe.com.punamba.backend_punamba.service;

import org.springframework.stereotype.Service;
import pe.com.punamba.backend_punamba.dto.IconoCategoriaRequestDTO;
import pe.com.punamba.backend_punamba.entity.IconoCategoria;
import pe.com.punamba.backend_punamba.repository.IconoCategoriaRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class IconoCategoriaService {

    private final IconoCategoriaRepository iconoCategoriaRepository;

    public IconoCategoriaService(IconoCategoriaRepository iconoCategoriaRepository) {
        this.iconoCategoriaRepository = iconoCategoriaRepository;
    }

    public List<IconoCategoria> listarTodos() {
        return iconoCategoriaRepository.findAllByOrderByNombreAsc();
    }

    public List<IconoCategoria> listarActivos() {
        return iconoCategoriaRepository.findByEstadoTrueOrderByNombreAsc();
    }

    public IconoCategoria crear(IconoCategoriaRequestDTO request) {
        IconoCategoriaRequestDTO requestSeguro = Objects.requireNonNull(request, "El request no puede ser null");

        String clase = limpiar(requestSeguro.getClaseCss());

        if (iconoCategoriaRepository.existsByClaseCss(clase)) {
            throw new RuntimeException("La clase CSS del icono ya existe");
        }

        Boolean estadoRequest = requestSeguro.getEstado();
        boolean estadoFinal = (estadoRequest != null) ? estadoRequest : true;

        IconoCategoria icono = new IconoCategoria();
        icono.setNombre(requestSeguro.getNombre() == null ? "" : requestSeguro.getNombre().trim());
        icono.setClaseCss(clase);
        icono.setEstado(estadoFinal);

        return iconoCategoriaRepository.save(icono);
    }

    public IconoCategoria editar(Integer id, IconoCategoriaRequestDTO request) {
        Integer idSeguro = Objects.requireNonNull(id, "El id del icono no puede ser null");
        IconoCategoriaRequestDTO requestSeguro = Objects.requireNonNull(request, "El request no puede ser null");

        IconoCategoria icono = iconoCategoriaRepository.findById(idSeguro)
                .orElseThrow(() -> new RuntimeException("Icono no encontrado"));

        String clase = limpiar(requestSeguro.getClaseCss());

        Optional<IconoCategoria> existenteOpt = iconoCategoriaRepository.findByClaseCss(clase);
        if (existenteOpt.isPresent()) {
            IconoCategoria existente = existenteOpt.get();
            if (!Objects.equals(existente.getIdIcono(), idSeguro)) {
                throw new RuntimeException("La clase CSS del icono ya existe");
            }
        }

        Boolean estadoRequest = requestSeguro.getEstado();
        boolean estadoFinal = (estadoRequest != null) ? estadoRequest : true;

        icono.setNombre(requestSeguro.getNombre() == null ? "" : requestSeguro.getNombre().trim());
        icono.setClaseCss(clase);
        icono.setEstado(estadoFinal);

        return iconoCategoriaRepository.save(icono);
    }

    public void eliminar(Integer id) {
        Integer idSeguro = Objects.requireNonNull(id, "El id del icono no puede ser null");

        if (!iconoCategoriaRepository.existsById(idSeguro)) {
            throw new RuntimeException("Icono no encontrado");
        }

        iconoCategoriaRepository.deleteById(idSeguro);
    }

    private String limpiar(String claseCss) {
        return claseCss == null ? "" : claseCss.trim();
    }
}