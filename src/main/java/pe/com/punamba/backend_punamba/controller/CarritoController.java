package pe.com.punamba.backend_punamba.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
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

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private CarritoMapper carritoMapper;

    private Usuario obtenerUsuario(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return usuario;
    }

    @GetMapping("/mi-carrito")
    public ResponseEntity<CarritoResponseDTO> obtener(Authentication auth) {
        Usuario usuarioLogueado = obtenerUsuario(auth);
        Carrito carrito = carritoService.obtenerPorUsuario(usuarioLogueado.getIdUsuario());
        return ResponseEntity.ok(carritoMapper.toResponse(carrito));
    }

    @PostMapping("/agregar")
    public ResponseEntity<CarritoResponseDTO> agregarProducto(
            @Valid @RequestBody CarritoItemRequestDTO request,
            Authentication auth) {
        Usuario usuarioLogueado = obtenerUsuario(auth);

        Carrito carrito = carritoService.agregarProducto(
                usuarioLogueado.getIdUsuario(),
                request.getIdVariante(),
                request.getCantidad());

        return ResponseEntity.ok(carritoMapper.toResponse(carrito));
    }

    @DeleteMapping("/item/{idVariante}")
    public ResponseEntity<Void> eliminarItem(@PathVariable Integer idVariante, Authentication auth) {
        Usuario usuarioLogueado = obtenerUsuario(auth);
        carritoService.eliminarItem(usuarioLogueado.getIdUsuario(), idVariante);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/limpiar")
    public ResponseEntity<Void> limpiar(Authentication auth) {
        Usuario usuarioLogueado = obtenerUsuario(auth);
        carritoService.limpiarCarrito(usuarioLogueado.getIdUsuario());
        return ResponseEntity.noContent().build();
    }
}