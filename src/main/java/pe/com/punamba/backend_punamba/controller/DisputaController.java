package pe.com.punamba.backend_punamba.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.DisputaMensajeRequestDTO;
import pe.com.punamba.backend_punamba.dto.DisputaMensajeResponseDTO;
import pe.com.punamba.backend_punamba.dto.DisputaRequestDTO;
import pe.com.punamba.backend_punamba.dto.DisputaResolverRequestDTO;
import pe.com.punamba.backend_punamba.dto.DisputaResponseDTO;
import pe.com.punamba.backend_punamba.entity.Disputa;
import pe.com.punamba.backend_punamba.entity.DisputaMensaje;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.mapper.DisputaMapper;
import pe.com.punamba.backend_punamba.service.DisputaService;

import java.util.List;
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
                disputaService.listarMisDisputas(usuarioLogueado)
                        .stream()
                        .map(disputaMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/admin/todas")
    public ResponseEntity<?> listarTodasAdmin(Authentication auth) {
        try {
            Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
            return ResponseEntity.ok(
                    disputaService.listarTodasAdmin(usuarioLogueado)
                            .stream()
                            .map(disputaMapper::toResponse)
                            .collect(Collectors.toList())
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/estado")
    public ResponseEntity<?> listarPorEstado(@RequestParam("estado") String estado, Authentication auth) {
        try {
            Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
            Disputa.EstadoDisputa estadoEnum = Disputa.EstadoDisputa.valueOf(estado);

            return ResponseEntity.ok(
                    disputaService.listarPorEstado(usuarioLogueado, estadoEnum)
                            .stream()
                            .map(disputaMapper::toResponse)
                            .collect(Collectors.toList())
            );
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Estado de disputa inválido", HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDetalle(@PathVariable Integer id, Authentication auth) {
        try {
            Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
            Disputa disputa = disputaService.obtenerPorIdParaActor(id, usuarioLogueado);
            return ResponseEntity.ok(disputaMapper.toResponse(disputa));
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
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
                    usuarioLogueado,
                    request.getIdOrdenDetalle(),
                    datosDisputa
            );

            return new ResponseEntity<>(disputaMapper.toResponse(nuevaDisputa), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/{id}/resolver")
    public ResponseEntity<?> resolverDisputa(
            @PathVariable Integer id,
            @RequestBody DisputaResolverRequestDTO request,
            Authentication auth
    ) {
        try {
            Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
            Disputa actualizada = disputaService.resolverDisputa(usuarioLogueado, id, request);
            return ResponseEntity.ok(disputaMapper.toResponse(actualizada));
        } catch (RuntimeException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "No se pudo resolver la disputa";
            HttpStatus status = msg.toLowerCase().contains("acceso") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(msg, status);
        }
    }

    @GetMapping("/{id}/mensajes")
    public ResponseEntity<?> listarMensajes(@PathVariable Integer id, Authentication auth) {
        try {
            Usuario usuarioLogueado = (Usuario) auth.getPrincipal();

            List<DisputaMensajeResponseDTO> response = disputaService.listarMensajes(usuarioLogueado, id)
                    .stream()
                    .map(this::toMensajeResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "No se pudieron listar los mensajes";
            HttpStatus status = msg.toLowerCase().contains("permiso") || msg.toLowerCase().contains("acceso")
                    ? HttpStatus.FORBIDDEN
                    : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(msg, status);
        }
    }

    @PostMapping("/{id}/mensajes")
    public ResponseEntity<?> enviarMensaje(
            @PathVariable Integer id,
            @Valid @RequestBody DisputaMensajeRequestDTO request,
            Authentication auth
    ) {
        try {
            Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
            DisputaMensaje mensaje = disputaService.enviarMensaje(usuarioLogueado, id, request);
            return new ResponseEntity<>(toMensajeResponse(mensaje), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "No se pudo enviar el mensaje";
            HttpStatus status = msg.toLowerCase().contains("permiso") || msg.toLowerCase().contains("acceso")
                    ? HttpStatus.FORBIDDEN
                    : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(msg, status);
        }
    }

    private DisputaMensajeResponseDTO toMensajeResponse(DisputaMensaje mensaje) {
        DisputaMensajeResponseDTO dto = new DisputaMensajeResponseDTO();
        dto.setIdMensaje(mensaje.getIdMensaje());
        dto.setIdDisputa(mensaje.getDisputa() != null ? mensaje.getDisputa().getIdDisputa() : null);
        dto.setIdUsuario(mensaje.getUsuario() != null ? mensaje.getUsuario().getIdUsuario() : null);
        dto.setUsuarioEmail(mensaje.getUsuario() != null ? mensaje.getUsuario().getEmail() : null);

        if (mensaje.getUsuario() != null) {
            dto.setNombreCompleto(
                    (mensaje.getUsuario().getNombre() != null ? mensaje.getUsuario().getNombre() : "")
                            + " "
                            + (mensaje.getUsuario().getApellido() != null ? mensaje.getUsuario().getApellido() : "")
            );
        }

        dto.setRolEmisor(mensaje.getRolEmisor());
        dto.setMensaje(mensaje.getMensaje());
        dto.setAdjuntoUrl(mensaje.getAdjuntoUrl());
        dto.setFechaEnvio(mensaje.getFechaEnvio());
        return dto;
    }
}