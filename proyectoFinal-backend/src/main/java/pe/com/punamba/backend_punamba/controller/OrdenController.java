package pe.com.punamba.backend_punamba.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.OrdenResponseDTO;
import pe.com.punamba.backend_punamba.entity.Orden;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.mapper.OrdenMapper;
import pe.com.punamba.backend_punamba.service.OrdenService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ordenes")
@CrossOrigin(origins = "*")
public class OrdenController {

    @Autowired private OrdenService ordenService;
    @Autowired private OrdenMapper ordenMapper;

    @PostMapping("/checkout")
    public ResponseEntity<OrdenResponseDTO> checkout(@RequestBody Map<String, Object> payload, Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        
        Integer idDireccion = (Integer) payload.get("idDireccion");
        String notas = (String) payload.get("notas");
        
        Orden nueva = ordenService.crearOrdenDesdeCarrito(usuarioLogueado.getIdUsuario(), idDireccion, notas);
        return ResponseEntity.ok(ordenMapper.toResponse(nueva));
    }

    @GetMapping("/mis-ordenes")
    public ResponseEntity<List<OrdenResponseDTO>> listarMisOrdenes(Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        List<OrdenResponseDTO> lista = ordenService.listarPorUsuario(usuarioLogueado.getIdUsuario()).stream()
                .map(ordenMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }
}