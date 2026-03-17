package pe.com.punamba.backend_punamba.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.AtributoRequestDTO;
import pe.com.punamba.backend_punamba.entity.Atributo;
import pe.com.punamba.backend_punamba.service.AtributoService;

import java.util.List;

@RestController
@RequestMapping("/api/atributos")
public class AtributoController {

    private final AtributoService atributoService;

    public AtributoController(AtributoService atributoService) {
        this.atributoService = atributoService;
    }

    @GetMapping
    public ResponseEntity<List<Atributo>> listar() {
        return ResponseEntity.ok(atributoService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody AtributoRequestDTO request) {
        try {
            Atributo atributo = atributoService.crear(
                    request.getNombre(),
                    request.getTipoDato(),
                    request.getUnidad()
            );
            return new ResponseEntity<>(atributo, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Integer id, @RequestBody AtributoRequestDTO request) {
        try {
            Atributo atributo = atributoService.editar(
                    id,
                    request.getNombre(),
                    request.getTipoDato(),
                    request.getUnidad()
            );
            return ResponseEntity.ok(atributo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
