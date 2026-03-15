package pe.com.punamba.backend_punamba.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import pe.com.punamba.backend_punamba.dto.TiendaRequestDTO;
import pe.com.punamba.backend_punamba.dto.TiendaResponseDTO;
import pe.com.punamba.backend_punamba.entity.Tienda;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.entity.Vendedor;
import pe.com.punamba.backend_punamba.mapper.TiendaMapper;
import pe.com.punamba.backend_punamba.service.FileStorageService;
import pe.com.punamba.backend_punamba.service.TiendaService;
import pe.com.punamba.backend_punamba.service.VendedorService;

@RestController
@RequestMapping("/api/tiendas")
public class TiendaController {

    @Autowired
    private TiendaService tiendaService;

    @Autowired
    private TiendaMapper tiendaMapper;

    @Autowired
    private VendedorService vendedorService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<TiendaResponseDTO>> listarActivas() {
        List<TiendaResponseDTO> tiendas = tiendaService.listarActivas().stream()
                .map(tiendaMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tiendas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TiendaResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return tiendaService.buscarPorId(id)
                .map(t -> ResponseEntity.ok(tiendaMapper.toResponse(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vendedor/{idVendedor}")
    public ResponseEntity<?> listarPorVendedor(@PathVariable Integer idVendedor) {
        return tiendaService.buscarPorVendedor(idVendedor)
                .<ResponseEntity<?>>map(t -> ResponseEntity.ok(tiendaMapper.toResponse(t)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("El vendedor no tiene tienda registrada"));
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> obtenerMiTienda(Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();

        Vendedor vendedor = vendedorService.buscarPorIdUsuario(usuarioLogueado.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("No tienes perfil de vendedor"));

        return tiendaService.buscarPorVendedor(vendedor.getIdVendedor())
                .<ResponseEntity<?>>map(t -> ResponseEntity.ok(tiendaMapper.toResponse(t)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("No tienes tienda registrada"));
    }

    @PostMapping("/perfil")
    public ResponseEntity<?> crearMiTienda(@Valid @RequestBody TiendaRequestDTO request, Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();

        Vendedor vendedor = vendedorService.buscarPorIdUsuario(usuarioLogueado.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("No tienes perfil de vendedor"));

        if (tiendaService.buscarPorVendedor(vendedor.getIdVendedor()).isPresent()) {
            return new ResponseEntity<>("Ya tienes una tienda registrada", HttpStatus.BAD_REQUEST);
        }

        if (tiendaService.existeNombre(request.getNombre())) {
            return new ResponseEntity<>("El nombre de la tienda ya está en uso", HttpStatus.BAD_REQUEST);
        }

        try {
            Tienda nuevaTienda = tiendaMapper.toEntity(request);
            Tienda tiendaGuardada = tiendaService.guardar(vendedor.getIdVendedor(), nuevaTienda);
            return new ResponseEntity<>(tiendaMapper.toResponse(tiendaGuardada), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> editarMiTienda(@Valid @RequestBody TiendaRequestDTO request, Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();

        Vendedor vendedor = vendedorService.buscarPorIdUsuario(usuarioLogueado.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("No tienes perfil de vendedor"));

        Tienda tienda = tiendaService.buscarPorVendedor(vendedor.getIdVendedor())
                .orElseThrow(() -> new RuntimeException("No tienes tienda registrada"));

        if (!tienda.getNombre().equals(request.getNombre()) && tiendaService.existeNombre(request.getNombre())) {
            return new ResponseEntity<>("El nombre de la tienda ya está en uso", HttpStatus.BAD_REQUEST);
        }

        tiendaMapper.updateEntityFromDto(request, tienda);
        Tienda tiendaActualizada = tiendaService.guardar(vendedor.getIdVendedor(), tienda);

        return new ResponseEntity<>(tiendaMapper.toResponse(tiendaActualizada), HttpStatus.OK);
    }

    @PostMapping("/perfil/banner")
    public ResponseEntity<?> subirBanner(@RequestParam("file") MultipartFile file, Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();

        Vendedor vendedor = vendedorService.buscarPorIdUsuario(usuarioLogueado.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("No tienes perfil de vendedor"));

        Tienda tienda = tiendaService.buscarPorVendedor(vendedor.getIdVendedor())
                .orElseThrow(() -> new RuntimeException("No tienes tienda registrada"));

        try {
            String nombreArchivo = Objects.requireNonNull(
                    fileStorageService.guardarArchivo(file),
                    "El nombre del archivo no puede ser null");

            String urlBanner = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .pathSegment(nombreArchivo)
                    .toUriString();

            tienda.setBanner(urlBanner);
            tiendaService.guardar(vendedor.getIdVendedor(), tienda);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Banner actualizado exitosamente",
                    "bannerUrl", urlBanner));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar el banner en el servidor.");
        }
    }

    @PostMapping("/vendedor/{idVendedor}")
    public ResponseEntity<?> crear(@PathVariable Integer idVendedor, @Valid @RequestBody TiendaRequestDTO request) {
        if (tiendaService.buscarPorVendedor(idVendedor).isPresent()) {
            return new ResponseEntity<>("Ese vendedor ya tiene una tienda registrada", HttpStatus.BAD_REQUEST);
        }

        if (tiendaService.existeNombre(request.getNombre())) {
            return new ResponseEntity<>("El nombre de la tienda ya está en uso", HttpStatus.BAD_REQUEST);
        }

        try {
            Tienda nuevaTienda = tiendaMapper.toEntity(request);
            Tienda tiendaGuardada = tiendaService.guardar(idVendedor, nuevaTienda);
            return new ResponseEntity<>(tiendaMapper.toResponse(tiendaGuardada), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Integer id, @Valid @RequestBody TiendaRequestDTO request) {
        return tiendaService.buscarPorId(id).map(t -> {

            if (!t.getNombre().equals(request.getNombre()) && tiendaService.existeNombre(request.getNombre())) {
                return new ResponseEntity<>("El nombre de la tienda ya está en uso", HttpStatus.BAD_REQUEST);
            }

            tiendaMapper.updateEntityFromDto(request, t);
            Tienda tiendaActualizada = tiendaService.guardar(t.getVendedor().getIdVendedor(), t);

            return new ResponseEntity<>(tiendaMapper.toResponse(tiendaActualizada), HttpStatus.OK);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        tiendaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}