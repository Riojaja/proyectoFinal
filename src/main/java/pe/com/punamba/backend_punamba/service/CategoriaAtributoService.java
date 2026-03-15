package pe.com.punamba.backend_punamba.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.dto.AtributoOpcionDTO;
import pe.com.punamba.backend_punamba.dto.CategoriaAtributoResponseDTO;
import pe.com.punamba.backend_punamba.entity.Atributo;
import pe.com.punamba.backend_punamba.entity.Categoria;
import pe.com.punamba.backend_punamba.entity.CategoriaAtributo;
import pe.com.punamba.backend_punamba.entity.CategoriaAtributoId;
import pe.com.punamba.backend_punamba.repository.AtributoRepository;
import pe.com.punamba.backend_punamba.repository.AtributoValorRepository;
import pe.com.punamba.backend_punamba.repository.CategoriaAtributoRepository;
import pe.com.punamba.backend_punamba.repository.CategoriaRepository;

import java.util.List;
import java.util.Objects;

@Service
public class CategoriaAtributoService {

    private final CategoriaAtributoRepository categoriaAtributoRepository;
    private final CategoriaRepository categoriaRepository;
    private final AtributoRepository atributoRepository;
    private final AtributoValorRepository atributoValorRepository;

    public CategoriaAtributoService(CategoriaAtributoRepository categoriaAtributoRepository,
            CategoriaRepository categoriaRepository,
            AtributoRepository atributoRepository,
            AtributoValorRepository atributoValorRepository) {
        this.categoriaAtributoRepository = categoriaAtributoRepository;
        this.categoriaRepository = categoriaRepository;
        this.atributoRepository = atributoRepository;
        this.atributoValorRepository = atributoValorRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaAtributoResponseDTO> listarPorCategoria(Integer idCategoria) {
        Integer idCategoriaSeguro = Objects.requireNonNull(idCategoria, "El id de la categoría no puede ser null");

        return categoriaAtributoRepository.findByCategoriaIdCategoriaOrderByOrdenAsc(idCategoriaSeguro)
                .stream()
                .map(ca -> {
                    CategoriaAtributoResponseDTO dto = new CategoriaAtributoResponseDTO();
                    dto.setIdAtributo(ca.getAtributo().getIdAtributo());
                    dto.setNombre(ca.getAtributo().getNombre());
                    dto.setTipoDato(ca.getAtributo().getTipoDato());
                    dto.setUnidad(ca.getAtributo().getUnidad());
                    dto.setObligatorio(ca.getObligatorio());
                    dto.setOrden(ca.getOrden());

                    List<AtributoOpcionDTO> opciones = atributoValorRepository
                            .findByAtributoIdAtributo(ca.getAtributo().getIdAtributo())
                            .stream()
                            .map(v -> new AtributoOpcionDTO(v.getIdValor(), v.getValor()))
                            .toList();

                    dto.setOpciones(opciones);
                    return dto;
                })
                .toList();
    }

    @Transactional
    public CategoriaAtributo asignar(Integer idCategoria, Integer idAtributo, Boolean obligatorio, Integer orden) {
        Integer idCategoriaSeguro = Objects.requireNonNull(idCategoria, "El id de la categoría no puede ser null");
        Integer idAtributoSeguro = Objects.requireNonNull(idAtributo, "El id del atributo no puede ser null");

        if (categoriaAtributoRepository.existsByCategoriaIdCategoriaAndAtributoIdAtributo(idCategoriaSeguro, idAtributoSeguro)) {
            throw new RuntimeException("El atributo ya está asignado a la categoría");
        }

        Categoria categoria = categoriaRepository.findById(idCategoriaSeguro)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Atributo atributo = atributoRepository.findById(idAtributoSeguro)
                .orElseThrow(() -> new RuntimeException("Atributo no encontrado"));

        CategoriaAtributo ca = new CategoriaAtributo();
        CategoriaAtributoId id = new CategoriaAtributoId();
        id.setIdCategoria(idCategoriaSeguro);
        id.setIdAtributo(idAtributoSeguro);

        ca.setId(id);
        ca.setCategoria(categoria);
        ca.setAtributo(atributo);
        ca.setObligatorio(obligatorio != null ? obligatorio : false);
        ca.setOrden(orden != null ? orden : 0);

        return categoriaAtributoRepository.save(ca);
    }

    @Transactional
    public CategoriaAtributo editar(Integer idCategoria, Integer idAtributo, Boolean obligatorio, Integer orden) {
        Integer idCategoriaSeguro = Objects.requireNonNull(idCategoria, "El id de la categoría no puede ser null");
        Integer idAtributoSeguro = Objects.requireNonNull(idAtributo, "El id del atributo no puede ser null");

        CategoriaAtributo ca = categoriaAtributoRepository
                .findByCategoriaIdCategoriaAndAtributoIdAtributo(idCategoriaSeguro, idAtributoSeguro)
                .orElseThrow(() -> new RuntimeException("Relación categoría-atributo no encontrada"));

        ca.setObligatorio(obligatorio != null ? obligatorio : ca.getObligatorio());
        ca.setOrden(orden != null ? orden : ca.getOrden());

        return categoriaAtributoRepository.save(ca);
    }

    @Transactional
    public void eliminar(Integer idCategoria, Integer idAtributo) {
        Integer idCategoriaSeguro = Objects.requireNonNull(idCategoria, "El id de la categoría no puede ser null");
        Integer idAtributoSeguro = Objects.requireNonNull(idAtributo, "El id del atributo no puede ser null");

        CategoriaAtributo ca = Objects.requireNonNull(
                categoriaAtributoRepository
                        .findByCategoriaIdCategoriaAndAtributoIdAtributo(idCategoriaSeguro, idAtributoSeguro)
                        .orElseThrow(() -> new RuntimeException("Relación categoría-atributo no encontrada")),
                "La relación categoría-atributo no puede ser null"
        );

        categoriaAtributoRepository.delete(ca);
    }
}