package pe.com.punamba.backend_punamba.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.com.punamba.backend_punamba.dto.RolesUpdateDTO;
import pe.com.punamba.backend_punamba.dto.UsuarioResponseDTO;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.mapper.UsuarioMapper;
import pe.com.punamba.backend_punamba.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    public UsuarioController(UsuarioService usuarioService, UsuarioMapper usuarioMapper) {
        this.usuarioService = usuarioService;
        this.usuarioMapper = usuarioMapper;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuarioActual(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(usuarioMapper.toResponse(usuario));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        List<UsuarioResponseDTO> usuarios = usuarioService.listarTodos()
                .stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Integer id) {
        return usuarioService.buscarPorId(id)
                .map(usuario -> ResponseEntity.ok(usuarioMapper.toResponse(usuario)))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(@PathVariable Integer id,
            @RequestBody Usuario detallesUsuario) {
        Usuario actualizado = usuarioService.actualizarPerfil(id, detallesUsuario);
        return ResponseEntity.ok(usuarioMapper.toResponse(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<UsuarioResponseDTO> actualizarRoles(@PathVariable Integer id,
            @RequestBody RolesUpdateDTO dto) {
        Usuario actualizado = usuarioService.actualizarRoles(id, dto.getRoles());
        return ResponseEntity.ok(usuarioMapper.toResponse(actualizado));
    }
}