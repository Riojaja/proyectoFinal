package pe.com.punamba.backend_punamba.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.DisputaRequestDTO;
import pe.com.punamba.backend_punamba.dto.DisputaResponseDTO;
import pe.com.punamba.backend_punamba.entity.Disputa;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.mapper.DisputaMapper;
import pe.com.punamba.backend_punamba.service.DisputaService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/disputas")
@CrossOrigin(origins = "*")
public class DisputaController {

    @Autowired private DisputaService disputaService;
    @Autowired private DisputaMapper disputaMapper;

    @GetMapping("/mis-disputas")
    public ResponseEntity<List<DisputaResponseDTO>> listarMisDisputas(Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(
                disputaService.listarPorUsuario(usuarioLogueado.getIdUsuario())
                        .stream()
                        .map(disputaMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/estado")
    public ResponseEntity<?> listarPorEstado(@RequestParam("estado") String estado) {
        try {
            Disputa.EstadoDisputa estadoEnum = Disputa.EstadoDisputa.valueOf(estado);
            return ResponseEntity.ok(
                    disputaService.listarPorEstado(estadoEnum)
                            .stream()
                            .map(disputaMapper::toResponse)
                            .collect(Collectors.toList())
            );
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Estado de disputa inválido", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/abrir")
    public ResponseEntity<?> abrirDisputa(@Valid @RequestBody DisputaRequestDTO request, Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        try {
            Disputa datosDisputa = new Disputa();
            datosDisputa.setMotivo(request.getMotivo());
            datosDisputa.setDescripcion(request.getDescripcion());
            datosDisputa.setEvidenciaJson(request.getEvidenciaJson());

            Disputa nuevaDisputa = disputaService.abrirDisputa(
                    usuarioLogueado.getIdUsuario(),
                    request.getIdOrdenDetalle(),
                    datosDisputa
            );

            return new ResponseEntity<>(disputaMapper.toResponse(nuevaDisputa), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/{id}/resolver")
    public ResponseEntity<?> resolverDisputa(@PathVariable Integer id, @RequestBody Map<String, String> payload) {
        try {
            String estadoString = payload.get("nuevoEstado");
            Disputa.EstadoDisputa nuevoEstado = Disputa.EstadoDisputa.valueOf(estadoString);
            return ResponseEntity.ok(
                    disputaMapper.toResponse(disputaService.resolverDisputa(id, nuevoEstado))
            );
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Estado de disputa inválido", HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}