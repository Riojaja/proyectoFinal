package pe.com.punamba.backend_punamba.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.com.punamba.backend_punamba.dto.CheckoutRequestDTO;
import pe.com.punamba.backend_punamba.dto.OrdenResponseDTO;
import pe.com.punamba.backend_punamba.entity.Orden;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.mapper.OrdenMapper;
import pe.com.punamba.backend_punamba.service.OrdenService;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@CrossOrigin(origins = "*")
public class OrdenController {

    @Autowired
    private OrdenService ordenService;

    @Autowired
    private OrdenMapper ordenMapper;

    private Usuario obtenerUsuario(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return usuario;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrdenResponseDTO> checkout(
            @RequestBody CheckoutRequestDTO request,
            Authentication auth) {

        Usuario usuarioLogueado = obtenerUsuario(auth);

        Orden orden = ordenService.crearDesdeCheckout(
                usuarioLogueado.getIdUsuario(),
                request.getIdDireccion(),
                request.getNotas(),
                request.getItems());

        OrdenResponseDTO dto = ordenMapper.toResponse(orden);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/mis-ordenes")
    public ResponseEntity<List<OrdenResponseDTO>> listarMisOrdenes(Authentication auth) {
        Usuario usuarioLogueado = obtenerUsuario(auth);
        return ResponseEntity.ok(ordenService.listarDtoPorUsuario(usuarioLogueado.getIdUsuario()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponseDTO> obtenerDetalle(@PathVariable Integer id, Authentication auth) {
        Usuario usuarioLogueado = obtenerUsuario(auth);
        return ResponseEntity.ok(
                ordenService.obtenerDtoPorIdDeUsuario(id, usuarioLogueado.getIdUsuario())
        );
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<OrdenResponseDTO> actualizarEstado(
            @PathVariable Integer id,
            @RequestParam String estado) {

        Orden orden = ordenService.actualizarEstado(id, estado);
        OrdenResponseDTO dto = ordenMapper.toResponse(orden);
        return ResponseEntity.ok(dto);
    }
}