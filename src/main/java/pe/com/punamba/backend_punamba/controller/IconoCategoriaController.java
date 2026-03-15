package pe.com.punamba.backend_punamba.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.IconoCategoriaRequestDTO;
import pe.com.punamba.backend_punamba.entity.IconoCategoria;
import pe.com.punamba.backend_punamba.service.IconoCategoriaService;

import java.util.List;

@RestController
@RequestMapping("/api/iconos-categoria")
public class IconoCategoriaController {

    private final IconoCategoriaService iconoCategoriaService;

    public IconoCategoriaController(IconoCategoriaService iconoCategoriaService) {
        this.iconoCategoriaService = iconoCategoriaService;
    }

    @GetMapping
    public ResponseEntity<List<IconoCategoria>> listarTodos() {
        return ResponseEntity.ok(iconoCategoriaService.listarTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<IconoCategoria>> listarActivos() {
        return ResponseEntity.ok(iconoCategoriaService.listarActivos());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody IconoCategoriaRequestDTO request) {
        try {
            return new ResponseEntity<>(iconoCategoriaService.crear(request), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Integer id, @RequestBody IconoCategoriaRequestDTO request) {
        try {
            return ResponseEntity.ok(iconoCategoriaService.editar(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            iconoCategoriaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}