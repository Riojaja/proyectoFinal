package pe.com.punamba.backend_punamba.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.TiendaRequestDTO;
import pe.com.punamba.backend_punamba.dto.TiendaResponseDTO;
import pe.com.punamba.backend_punamba.entity.Tienda;
import pe.com.punamba.backend_punamba.mapper.TiendaMapper;
import pe.com.punamba.backend_punamba.service.TiendaService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tiendas")
@CrossOrigin(origins = "*")
public class TiendaController {

    @Autowired private TiendaService tiendaService;
    @Autowired private TiendaMapper tiendaMapper;

    @GetMapping
    public ResponseEntity<List<TiendaResponseDTO>> listarActivas() {
        List<TiendaResponseDTO> tiendas = tiendaService.listarActivas().stream()
                .map(tiendaMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tiendas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TiendaResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return tiendaService.buscarPorId(id)
                .map(t -> ResponseEntity.ok(tiendaMapper.toResponse(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vendedor/{idVendedor}")
    public ResponseEntity<List<TiendaResponseDTO>> listarPorVendedor(@PathVariable Integer idVendedor) {
        List<TiendaResponseDTO> tiendas = tiendaService.buscarPorVendedor(idVendedor).stream()
                .map(tiendaMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tiendas);
    }

    @PostMapping("/vendedor/{idVendedor}")
    public ResponseEntity<?> crear(@PathVariable Integer idVendedor, @Valid @RequestBody TiendaRequestDTO request) {
        if (tiendaService.existeNombre(request.getNombre())) {
            return new ResponseEntity<>("El nombre de la tienda ya está en uso", HttpStatus.BAD_REQUEST);
        }

        try {
            Tienda nuevaTienda = tiendaMapper.toEntity(request);
            Tienda tiendaGuardada = tiendaService.guardar(idVendedor, nuevaTienda);
            return new ResponseEntity<>(tiendaMapper.toResponse(tiendaGuardada), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Integer id, @Valid @RequestBody TiendaRequestDTO request) {
        return tiendaService.buscarPorId(id).map(t -> {
            
            if (!t.getNombre().equals(request.getNombre()) && tiendaService.existeNombre(request.getNombre())) {
                return new ResponseEntity<>("El nombre de la tienda ya está en uso", HttpStatus.BAD_REQUEST);
            }

            tiendaMapper.updateEntityFromDto(request, t);
            Tienda tiendaActualizada = tiendaService.guardar(t.getVendedor().getIdVendedor(), t);
            
            return new ResponseEntity<>(tiendaMapper.toResponse(tiendaActualizada), HttpStatus.OK);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        tiendaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}