package pe.com.punamba.backend_punamba.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.CategoriaPromoDTO;
import pe.com.punamba.backend_punamba.dto.CategoriaRequestDTO;
import pe.com.punamba.backend_punamba.dto.CategoriaResponseDTO;
import pe.com.punamba.backend_punamba.entity.Categoria;
import pe.com.punamba.backend_punamba.mapper.CategoriaMapper;
import pe.com.punamba.backend_punamba.service.CategoriaService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final CategoriaMapper categoriaMapper;

    public CategoriaController(CategoriaService categoriaService, CategoriaMapper categoriaMapper) {
        this.categoriaService = categoriaService;
        this.categoriaMapper = categoriaMapper;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarPrincipales() {
        List<CategoriaResponseDTO> lista = categoriaService.listarPrincipales().stream()
                .map(categoriaMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/todas")
    public ResponseEntity<List<CategoriaResponseDTO>> listarTodas() {
        List<CategoriaResponseDTO> lista = categoriaService.listarTodas().stream()
                .map(categoriaMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/promos")
    public ResponseEntity<List<CategoriaPromoDTO>> listarPromos(
            @RequestParam(required = false, defaultValue = "3") Integer limit) {
        return ResponseEntity.ok(categoriaService.listarPromosDestacadas(limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return categoriaService.buscarPorId(id)
                .map(c -> ResponseEntity.ok(categoriaMapper.toResponse(c)))
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
    }

    @GetMapping("/{id}/subcategorias")
    public ResponseEntity<List<CategoriaResponseDTO>> listarSubcategorias(@PathVariable Integer id) {
        List<CategoriaResponseDTO> lista = categoriaService.listarSubcategorias(id).stream()
                .map(categoriaMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crear(@Valid @RequestBody CategoriaRequestDTO request) {
        if (categoriaService.existeNombre(request.getNombre())) {
            throw new RuntimeException("El nombre de esta categoría ya existe");
        }

        Categoria nuevaCategoria = categoriaMapper.toEntity(request);

        if (request.getIdCategoriaPadre() != null) {
            Categoria padre = categoriaService.buscarPorId(request.getIdCategoriaPadre())
                    .orElseThrow(() -> new RuntimeException("Categoría padre no encontrada"));
            nuevaCategoria.setCategoriaPadre(padre);
        }

        Categoria guardada = categoriaService.guardar(nuevaCategoria);
        return new ResponseEntity<>(categoriaMapper.toResponse(guardada), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> editar(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaRequestDTO request) {
        Categoria categoriaExistente = categoriaService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        if (!categoriaExistente.getNombre().equals(request.getNombre())
                && categoriaService.existeNombre(request.getNombre())) {
            throw new RuntimeException("El nombre de la categoría ya está en uso");
        }

        categoriaMapper.updateEntityFromDto(request, categoriaExistente);

        if (request.getIdCategoriaPadre() != null) {
            Categoria padre = categoriaService.buscarPorId(request.getIdCategoriaPadre())
                    .orElseThrow(() -> new RuntimeException("Categoría padre no encontrada"));
            categoriaExistente.setCategoriaPadre(padre);
        } else {
            categoriaExistente.setCategoriaPadre(null);
        }

        Categoria actualizada = categoriaService.guardar(categoriaExistente);
        return ResponseEntity.ok(categoriaMapper.toResponse(actualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
