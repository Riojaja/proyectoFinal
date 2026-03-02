package pe.com.punamba.backend_punamba.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.FavoritoRequestDTO;
import pe.com.punamba.backend_punamba.dto.FavoritoResponseDTO;
import pe.com.punamba.backend_punamba.entity.Favorito;
import pe.com.punamba.backend_punamba.mapper.FavoritoMapper;
import pe.com.punamba.backend_punamba.service.FavoritoService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favoritos")
@CrossOrigin(origins = "*")
public class FavoritoController {

    @Autowired private FavoritoService favoritoService;
    @Autowired private FavoritoMapper favoritoMapper;

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<FavoritoResponseDTO>> listar(@PathVariable Integer idUsuario) {
        List<FavoritoResponseDTO> lista = favoritoService.listarPorUsuario(idUsuario).stream()
                .map(favoritoMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/verificar/usuario/{idUsuario}/producto/{idProducto}")
    public ResponseEntity<Boolean> verificarFavorito(@PathVariable Integer idUsuario, @PathVariable Integer idProducto) {
        return ResponseEntity.ok(favoritoService.verificarSiEsFavorito(idUsuario, idProducto));
    }

    @PostMapping
    public ResponseEntity<?> agregar(@RequestBody FavoritoRequestDTO request) {
        if (request.getIdUsuario() == null || request.getIdProducto() == null) {
            return new ResponseEntity<>("Faltan datos de usuario o producto", HttpStatus.BAD_REQUEST);
        }

        try {
            Favorito nuevoFavorito = favoritoService.agregar(request.getIdUsuario(), request.getIdProducto());
            return new ResponseEntity<>(favoritoMapper.toResponse(nuevoFavorito), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/usuario/{idUsuario}/producto/{idProducto}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer idUsuario, @PathVariable Integer idProducto) {
        favoritoService.eliminar(idUsuario, idProducto);
        return ResponseEntity.noContent().build();
    }
}