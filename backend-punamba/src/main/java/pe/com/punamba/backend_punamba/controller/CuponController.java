package pe.com.punamba.backend_punamba.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.CuponRequestDTO;
import pe.com.punamba.backend_punamba.dto.CuponResponseDTO;
import pe.com.punamba.backend_punamba.entity.Cupon;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.entity.Vendedor;
import pe.com.punamba.backend_punamba.mapper.CuponMapper;
import pe.com.punamba.backend_punamba.service.CuponService;
import pe.com.punamba.backend_punamba.service.VendedorService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cupones")
@CrossOrigin(origins = "*")
public class CuponController {

    @Autowired private CuponService cuponService;
    @Autowired private VendedorService vendedorService;
    @Autowired private CuponMapper cuponMapper;

    @GetMapping
    public ResponseEntity<List<CuponResponseDTO>> listarTodos() {
        List<CuponResponseDTO> lista = cuponService.listarTodos().stream()
                .map(cuponMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuponResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return cuponService.buscarPorId(id)
                .map(c -> ResponseEntity.ok(cuponMapper.toResponse(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vendedor/{idVendedor}")
    public ResponseEntity<List<CuponResponseDTO>> listarPorVendedor(@PathVariable Integer idVendedor) {
        List<CuponResponseDTO> lista = cuponService.listarPorVendedor(idVendedor).stream()
                .map(cuponMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/validar")
    public ResponseEntity<?> validar(@RequestParam String codigo, @RequestParam BigDecimal subtotal) {
        try {
            Cupon cuponValido = cuponService.validarCupon(codigo.toUpperCase(), subtotal);
            return ResponseEntity.ok(cuponMapper.toResponse(cuponValido));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody CuponRequestDTO request, Authentication auth) {
        if (cuponService.existeCodigo(request.getCodigo().toUpperCase())) {
            return new ResponseEntity<>("Ya existe un cupón con este código", HttpStatus.BAD_REQUEST);
        }

        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        Vendedor vendedor = vendedorService.buscarPorIdUsuario(usuarioLogueado.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("No tienes perfil de vendedor para crear cupones."));

        try {
            Cupon nuevoCupon = cuponMapper.toEntity(request);
            nuevoCupon.setVendedor(vendedor);
            Cupon guardado = cuponService.crearCupon(nuevoCupon);
            return new ResponseEntity<>(cuponMapper.toResponse(guardado), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Integer id, @Valid @RequestBody CuponRequestDTO request) {
        return cuponService.buscarPorId(id).map(c -> {
            if (!c.getCodigo().equalsIgnoreCase(request.getCodigo()) && cuponService.existeCodigo(request.getCodigo().toUpperCase())) {
                return new ResponseEntity<>("Ya existe un cupón con este código", HttpStatus.BAD_REQUEST);
            }

            cuponMapper.updateEntityFromDto(request, c);
            try {
                return new ResponseEntity<>(cuponMapper.toResponse(cuponService.actualizar(c)), HttpStatus.OK);
            } catch (RuntimeException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        cuponService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}