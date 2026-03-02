package pe.com.punamba.backend_punamba.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.MetodoEnvioDTO;
import pe.com.punamba.backend_punamba.entity.MetodoEnvio;
import pe.com.punamba.backend_punamba.mapper.MetodoEnvioMapper;
import pe.com.punamba.backend_punamba.service.MetodoEnvioService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/metodos-envio")
@CrossOrigin(origins = "*")
public class MetodoEnvioController {

    @Autowired private MetodoEnvioService metodoEnvioService;
    @Autowired private MetodoEnvioMapper metodoEnvioMapper;

    @GetMapping("/activos")
    public ResponseEntity<List<MetodoEnvioDTO>> listarActivos() {
        return ResponseEntity.ok(metodoEnvioService.listarActivos().stream()
                .map(metodoEnvioMapper::toDto).collect(Collectors.toList()));
    }

    @GetMapping
    public ResponseEntity<List<MetodoEnvioDTO>> listarTodos() {
        return ResponseEntity.ok(metodoEnvioService.listarTodos().stream()
                .map(metodoEnvioMapper::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetodoEnvioDTO> obtenerPorId(@PathVariable Integer id) {
        return metodoEnvioService.buscarPorId(id)
                .map(m -> ResponseEntity.ok(metodoEnvioMapper.toDto(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MetodoEnvioDTO> crear(@Valid @RequestBody MetodoEnvioDTO request) {
        MetodoEnvio guardado = metodoEnvioService.guardar(metodoEnvioMapper.toEntity(request));
        return new ResponseEntity<>(metodoEnvioMapper.toDto(guardado), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetodoEnvioDTO> editar(@PathVariable Integer id, @Valid @RequestBody MetodoEnvioDTO request) {
        return metodoEnvioService.buscarPorId(id).map(m -> {
            m.setNombre(request.getNombre());
            m.setDescripcion(request.getDescripcion());
            m.setTiempoEstimado(request.getTiempoEstimado());
            m.setEstado(request.getEstado());
            return ResponseEntity.ok(metodoEnvioMapper.toDto(metodoEnvioService.guardar(m)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        metodoEnvioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}