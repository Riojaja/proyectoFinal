package pe.com.punamba.backend_punamba.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import pe.com.punamba.backend_punamba.dto.DireccionRequestDTO;
import pe.com.punamba.backend_punamba.dto.DireccionResponseDTO;
import pe.com.punamba.backend_punamba.entity.Direccion;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.mapper.DireccionMapper;
import pe.com.punamba.backend_punamba.service.DireccionService;

@RestController
@RequestMapping("/api/direcciones")
@CrossOrigin(origins = "*")
public class DireccionController {

    @Autowired
    private DireccionService direccionService;

    @Autowired
    private DireccionMapper direccionMapper;

    @GetMapping("/mis-direcciones")
    public ResponseEntity<List<DireccionResponseDTO>> listarMisDirecciones(Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();

        List<DireccionResponseDTO> lista = direccionService
                .listarPorUsuario(usuarioLogueado.getIdUsuario())
                .stream()
                .map(direccionMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<DireccionResponseDTO>> listarPorUsuario(@PathVariable Integer idUsuario) {
        List<DireccionResponseDTO> lista = direccionService
                .listarPorUsuario(idUsuario)
                .stream()
                .map(direccionMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DireccionResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return direccionService.buscarPorId(id)
                .map(d -> ResponseEntity.ok(direccionMapper.toResponse(d)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody DireccionRequestDTO request, Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();

        try {
            Direccion nuevaDireccion = direccionMapper.toEntity(request);

            Direccion guardada = direccionService.guardarNueva(
                    usuarioLogueado.getIdUsuario(),
                    request.getCodigoUbigeo(),
                    nuevaDireccion
            );

            return new ResponseEntity<>(direccionMapper.toResponse(guardada), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/usuario/{idUsuario}/ubigeo/{codigoUbigeo}")
    public ResponseEntity<?> crearPorUsuarioYUbigeo(
            @PathVariable Integer idUsuario,
            @PathVariable String codigoUbigeo,
            @Valid @RequestBody DireccionRequestDTO request
    ) {
        try {
            Direccion nuevaDireccion = direccionMapper.toEntity(request);

            Direccion guardada = direccionService.guardarNueva(
                    idUsuario,
                    codigoUbigeo,
                    nuevaDireccion
            );

            return new ResponseEntity<>(direccionMapper.toResponse(guardada), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Integer id, @Valid @RequestBody DireccionRequestDTO request) {
        try {
            Direccion detalles = direccionMapper.toEntity(request);

            Direccion actualizada = direccionService.actualizar(
                    id,
                    request.getCodigoUbigeo(),
                    detalles
            );

            return ResponseEntity.ok(direccionMapper.toResponse(actualizada));
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        direccionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}