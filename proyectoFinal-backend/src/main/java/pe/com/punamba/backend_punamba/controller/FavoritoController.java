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
       
        List<Favorito> favoritos = favoritoService.listarPorUsuario(idUsuario);
        List<FavoritoResponseDTO> lista = favoritos.stream()
                .map(favorito -> {
                    System.out.println("🔵 Procesando producto: " + favorito.getProducto().getNombre());
                    System.out.println("🔵 Precio en BD: " + favorito.getProducto().getPrecioBase());
                    System.out.println("🔵 Imágenes en BD: " + 
                        (favorito.getProducto().getImagenes() != null ? 
                         favorito.getProducto().getImagenes().size() : 0));
                    
                    return favoritoMapper.toResponse(favorito);
                })
                .collect(Collectors.toList());
        
        // Verificar el primer elemento
        if (!lista.isEmpty()) {
            System.out.println("📦 [FAVORITO] PRIMER DTO GENERADO:");
            System.out.println("  - idProducto: " + lista.get(0).getIdProducto());
            System.out.println("  - nombre: " + lista.get(0).getNombreProducto());
            System.out.println("  - precio: " + lista.get(0).getPrecioProducto());
            System.out.println("  - imagen: " + lista.get(0).getImagenProducto());
        }
        
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/verificar/usuario/{idUsuario}/producto/{idProducto}")
    public ResponseEntity<Boolean> verificarFavorito(@PathVariable Integer idUsuario, @PathVariable Integer idProducto) {
        System.out.println("🔵 [FAVORITO] Verificando favorito - Usuario: " + idUsuario + ", Producto: " + idProducto);
        boolean esFavorito = favoritoService.verificarSiEsFavorito(idUsuario, idProducto);
        System.out.println("🔵 [FAVORITO] Resultado: " + esFavorito);
        return ResponseEntity.ok(esFavorito);
    }

    @PostMapping
    public ResponseEntity<?> agregar(@RequestBody FavoritoRequestDTO request) {
        System.out.println("🔵 [FAVORITO] Agregando favorito - Usuario: " + request.getIdUsuario() + ", Producto: " + request.getIdProducto());
        
        if (request.getIdUsuario() == null || request.getIdProducto() == null) {
            return new ResponseEntity<>("Faltan datos de usuario o producto", HttpStatus.BAD_REQUEST);
        }

        try {
            Favorito nuevoFavorito = favoritoService.agregar(request.getIdUsuario(), request.getIdProducto());
            FavoritoResponseDTO response = favoritoMapper.toResponse(nuevoFavorito);
            System.out.println("✅ [FAVORITO] Agregado correctamente");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            System.out.println("🔴 [FAVORITO] Error al agregar: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/usuario/{idUsuario}/producto/{idProducto}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer idUsuario, @PathVariable Integer idProducto) {
        System.out.println("🔵 [FAVORITO] Eliminando favorito - Usuario: " + idUsuario + ", Producto: " + idProducto);
        favoritoService.eliminar(idUsuario, idProducto);
        System.out.println("✅ [FAVORITO] Eliminado correctamente");
        return ResponseEntity.noContent().build();
    }
}