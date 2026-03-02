package pe.com.punamba.backend_punamba.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.CarritoItemRequestDTO;
import pe.com.punamba.backend_punamba.dto.CarritoResponseDTO;
import pe.com.punamba.backend_punamba.entity.Carrito;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.mapper.CarritoMapper;
import pe.com.punamba.backend_punamba.service.CarritoService;

@RestController
@RequestMapping("/api/carrito")
@CrossOrigin(origins = "*")
public class CarritoController {

    @Autowired private CarritoService carritoService;
    @Autowired private CarritoMapper carritoMapper;

    @GetMapping("/mi-carrito")
    public ResponseEntity<CarritoResponseDTO> obtener(Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        Carrito carrito = carritoService.obtenerPorUsuario(usuarioLogueado.getIdUsuario());
        return ResponseEntity.ok(carritoMapper.toResponse(carrito));
    }

    @PostMapping("/agregar")
    public ResponseEntity<CarritoResponseDTO> agregarProducto(@Valid @RequestBody CarritoItemRequestDTO request, Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        Carrito carrito = carritoService.agregarProducto(
                usuarioLogueado.getIdUsuario(), 
                request.getIdVariante(), 
                request.getCantidad()
        );
        return ResponseEntity.ok(carritoMapper.toResponse(carrito));
    }

    @DeleteMapping("/item/{idVariante}")
    public ResponseEntity<Void> eliminarItem(@PathVariable Integer idVariante, Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        carritoService.eliminarItem(usuarioLogueado.getIdUsuario(), idVariante);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/limpiar")
    public ResponseEntity<Void> limpiar(Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        carritoService.limpiarCarrito(usuarioLogueado.getIdUsuario());
        return ResponseEntity.noContent().build();
    }
}