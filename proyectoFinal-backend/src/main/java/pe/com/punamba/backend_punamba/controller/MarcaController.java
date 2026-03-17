package pe.com.punamba.backend_punamba.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.MarcaRequestDTO;
import pe.com.punamba.backend_punamba.entity.Marca;
import pe.com.punamba.backend_punamba.service.MarcaService;

import java.util.List;

@RestController
@RequestMapping("/api/marcas")
public class MarcaController {

    private final MarcaService marcaService;

    public MarcaController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    @GetMapping
    public ResponseEntity<List<Marca>> listar() {
        return ResponseEntity.ok(marcaService.listarTodas());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<Marca>> listarActivas() {
        return ResponseEntity.ok(marcaService.listarActivas());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody MarcaRequestDTO request) {
        try {
            Marca marca = marcaService.crear(request.getNombre(), request.getEstado());
            return new ResponseEntity<>(marca, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Integer id, @RequestBody MarcaRequestDTO request) {
        try {
            Marca marca = marcaService.editar(id, request.getNombre(), request.getEstado());
            return ResponseEntity.ok(marca);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}