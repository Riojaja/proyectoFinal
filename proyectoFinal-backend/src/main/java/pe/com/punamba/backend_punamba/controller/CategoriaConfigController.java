package pe.com.punamba.backend_punamba.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.CategoriaAtributoRequestDTO;
import pe.com.punamba.backend_punamba.dto.CategoriaAtributoResponseDTO;
import pe.com.punamba.backend_punamba.dto.CategoriaMarcaRequestDTO;
import pe.com.punamba.backend_punamba.dto.MarcaResponseDTO;
import pe.com.punamba.backend_punamba.service.CategoriaAtributoService;
import pe.com.punamba.backend_punamba.service.CategoriaMarcaService;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaConfigController {

    private final CategoriaMarcaService categoriaMarcaService;
    private final CategoriaAtributoService categoriaAtributoService;

    public CategoriaConfigController(CategoriaMarcaService categoriaMarcaService,
                                     CategoriaAtributoService categoriaAtributoService) {
        this.categoriaMarcaService = categoriaMarcaService;
        this.categoriaAtributoService = categoriaAtributoService;
    }

    @GetMapping("/{idCategoria}/marcas")
    public ResponseEntity<List<MarcaResponseDTO>> listarMarcasPorCategoria(@PathVariable Integer idCategoria) {
        return ResponseEntity.ok(categoriaMarcaService.listarMarcasPorCategoria(idCategoria));
    }

    @GetMapping("/{idCategoria}/atributos")
    public ResponseEntity<List<CategoriaAtributoResponseDTO>> listarAtributosPorCategoria(@PathVariable Integer idCategoria) {
        return ResponseEntity.ok(categoriaAtributoService.listarPorCategoria(idCategoria));
    }

    @PostMapping("/marcas")
    public ResponseEntity<?> asignarMarca(@RequestBody CategoriaMarcaRequestDTO request) {
        try {
            return new ResponseEntity<>(
                    categoriaMarcaService.asignar(request.getIdCategoria(), request.getIdMarca()),
                    HttpStatus.CREATED
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{idCategoria}/marcas/{idMarca}")
    public ResponseEntity<?> eliminarMarca(@PathVariable Integer idCategoria, @PathVariable Integer idMarca) {
        try {
            categoriaMarcaService.eliminar(idCategoria, idMarca);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/atributos")
    public ResponseEntity<?> asignarAtributo(@RequestBody CategoriaAtributoRequestDTO request) {
        try {
            return new ResponseEntity<>(
                    categoriaAtributoService.asignar(
                            request.getIdCategoria(),
                            request.getIdAtributo(),
                            request.getObligatorio(),
                            request.getOrden()
                    ),
                    HttpStatus.CREATED
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/atributos")
    public ResponseEntity<?> editarAtributo(@RequestBody CategoriaAtributoRequestDTO request) {
        try {
            return ResponseEntity.ok(
                    categoriaAtributoService.editar(
                            request.getIdCategoria(),
                            request.getIdAtributo(),
                            request.getObligatorio(),
                            request.getOrden()
                    )
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{idCategoria}/atributos/{idAtributo}")
    public ResponseEntity<?> eliminarAtributo(@PathVariable Integer idCategoria, @PathVariable Integer idAtributo) {
        try {
            categoriaAtributoService.eliminar(idCategoria, idAtributo);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}