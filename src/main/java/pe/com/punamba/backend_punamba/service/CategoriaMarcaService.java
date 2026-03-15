package pe.com.punamba.backend_punamba.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.dto.MarcaResponseDTO;
import pe.com.punamba.backend_punamba.entity.Categoria;
import pe.com.punamba.backend_punamba.entity.CategoriaMarca;
import pe.com.punamba.backend_punamba.entity.CategoriaMarcaId;
import pe.com.punamba.backend_punamba.entity.Marca;
import pe.com.punamba.backend_punamba.repository.CategoriaMarcaRepository;
import pe.com.punamba.backend_punamba.repository.CategoriaRepository;
import pe.com.punamba.backend_punamba.repository.MarcaRepository;

import java.util.List;
import java.util.Objects;

@Service
public class CategoriaMarcaService {

    private final CategoriaMarcaRepository categoriaMarcaRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;

    public CategoriaMarcaService(CategoriaMarcaRepository categoriaMarcaRepository,
                                 CategoriaRepository categoriaRepository,
                                 MarcaRepository marcaRepository) {
        this.categoriaMarcaRepository = categoriaMarcaRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
    }

    @Transactional(readOnly = true)
    public List<MarcaResponseDTO> listarMarcasPorCategoria(Integer idCategoria) {
        Integer idCategoriaSeguro = Objects.requireNonNull(idCategoria, "El id de la categoría no puede ser null");

        return categoriaMarcaRepository.findByCategoriaIdCategoria(idCategoriaSeguro)
                .stream()
                .map(cm -> new MarcaResponseDTO(
                        cm.getMarca().getIdMarca(),
                        cm.getMarca().getNombre(),
                        cm.getMarca().getEstado()
                ))
                .toList();
    }

    @Transactional
    public CategoriaMarca asignar(Integer idCategoria, Integer idMarca) {
        Integer idCategoriaSeguro = Objects.requireNonNull(idCategoria, "El id de la categoría no puede ser null");
        Integer idMarcaSeguro = Objects.requireNonNull(idMarca, "El id de la marca no puede ser null");

        if (categoriaMarcaRepository.existsByCategoriaIdCategoriaAndMarcaIdMarca(idCategoriaSeguro, idMarcaSeguro)) {
            throw new RuntimeException("La marca ya está asignada a la categoría");
        }

        Categoria categoria = categoriaRepository.findById(idCategoriaSeguro)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Marca marca = marcaRepository.findById(idMarcaSeguro)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada"));

        CategoriaMarca cm = new CategoriaMarca();
        CategoriaMarcaId id = new CategoriaMarcaId();
        id.setIdCategoria(idCategoriaSeguro);
        id.setIdMarca(idMarcaSeguro);

        cm.setId(id);
        cm.setCategoria(categoria);
        cm.setMarca(marca);

        return categoriaMarcaRepository.save(cm);
    }

    @Transactional
    public void eliminar(Integer idCategoria, Integer idMarca) {
        Integer idCategoriaSeguro = Objects.requireNonNull(idCategoria, "El id de la categoría no puede ser null");
        Integer idMarcaSeguro = Objects.requireNonNull(idMarca, "El id de la marca no puede ser null");

        CategoriaMarca cm = Objects.requireNonNull(
                categoriaMarcaRepository
                        .findByCategoriaIdCategoriaAndMarcaIdMarca(idCategoriaSeguro, idMarcaSeguro)
                        .orElseThrow(() -> new RuntimeException("Relación categoría-marca no encontrada")),
                "La relación categoría-marca no puede ser null"
        );

        categoriaMarcaRepository.delete(cm);
    }
}