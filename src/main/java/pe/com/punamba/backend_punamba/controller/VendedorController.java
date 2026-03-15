package pe.com.punamba.backend_punamba.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import pe.com.punamba.backend_punamba.dto.AdminVendedorModeracionDTO;
import pe.com.punamba.backend_punamba.dto.VendedorRequestDTO;
import pe.com.punamba.backend_punamba.dto.VendedorResponseDTO;
import pe.com.punamba.backend_punamba.dto.VendedorUpdateDTO;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.entity.Vendedor;
import pe.com.punamba.backend_punamba.mapper.VendedorMapper;
import pe.com.punamba.backend_punamba.service.FileStorageService;
import pe.com.punamba.backend_punamba.service.VendedorService;

@RestController
@RequestMapping("/api/vendedores")
public class VendedorController {

    private final VendedorService vendedorService;
    private final VendedorMapper vendedorMapper;
    private final FileStorageService fileStorageService;

    public VendedorController(
            VendedorService vendedorService,
            VendedorMapper vendedorMapper,
            FileStorageService fileStorageService) {
        this.vendedorService = vendedorService;
        this.vendedorMapper = vendedorMapper;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<List<AdminVendedorModeracionDTO>> listar() {
        return ResponseEntity.ok(vendedorService.listarParaModeracion());
    }

    @GetMapping("/perfil")
    public ResponseEntity<VendedorResponseDTO> miPerfil(Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();

        Vendedor vendedor = vendedorService.buscarPorIdUsuario(usuarioLogueado.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("No tienes perfil de vendedor"));

        return ResponseEntity.ok(vendedorMapper.toResponse(vendedor));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendedorResponseDTO> obtener(@PathVariable Integer id) {
        return vendedorService.buscarPorId(id)
                .map(v -> ResponseEntity.ok(vendedorMapper.toResponse(v)))
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
    }

    @PostMapping
    public ResponseEntity<VendedorResponseDTO> crear(
            @Valid @RequestBody VendedorRequestDTO request,
            Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();

        if (vendedorService.usuarioYaEsVendedor(usuarioLogueado.getIdUsuario())) {
            throw new RuntimeException("Ya tienes una cuenta de vendedor registrada.");
        }
        if (request.getRuc() != null && vendedorService.existeRuc(request.getRuc())) {
            throw new RuntimeException("El RUC ya se encuentra registrado.");
        }
        if (vendedorService.existeNombreTienda(request.getNombreTienda())) {
            throw new RuntimeException("El nombre de la tienda ya está en uso.");
        }

        Vendedor nuevoVendedor = vendedorMapper.toEntity(request);
        nuevoVendedor.setUsuario(usuarioLogueado);

        Vendedor guardado = vendedorService.guardarNuevo(nuevoVendedor);
        return new ResponseEntity<>(vendedorMapper.toResponse(guardado), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendedorResponseDTO> editar(
            @PathVariable Integer id,
            @Valid @RequestBody VendedorRequestDTO request,
            Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();

        Vendedor vendedorExistente = vendedorService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        boolean esDueno = vendedorExistente.getUsuario().getIdUsuario().equals(usuarioLogueado.getIdUsuario());
        boolean esAdmin = usuarioLogueado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getNombre()));

        if (!esDueno && !esAdmin) {
            throw new RuntimeException("No tienes permiso para editar esta tienda.");
        }

        vendedorMapper.updateEntityFromDto(request, vendedorExistente);
        Vendedor actualizado = vendedorService.actualizar(vendedorExistente);

        return ResponseEntity.ok(vendedorMapper.toResponse(actualizado));
    }

    @PutMapping("/perfil")
    public ResponseEntity<VendedorResponseDTO> actualizarPerfil(
            Authentication auth,
            @RequestBody VendedorUpdateDTO dto) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();

        Vendedor vendedor = vendedorService.buscarPorIdUsuario(usuarioLogueado.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("No tienes perfil de vendedor"));

        if (dto.getNombre() != null && !dto.getNombre().trim().isEmpty()) {
            vendedor.setNombreTienda(dto.getNombre().trim());
        }

        vendedor.setDescripcion(dto.getDescripcion());

        if (dto.getEstado() != null) {
            vendedor.setEstado(dto.getEstado()
                    ? Vendedor.EstadoVendedor.activo
                    : Vendedor.EstadoVendedor.inactivo);
        }

        Vendedor actualizado = vendedorService.actualizar(vendedor);

        return ResponseEntity.ok(vendedorMapper.toResponse(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();

        Vendedor vendedorExistente = vendedorService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        boolean esDueno = vendedorExistente.getUsuario().getIdUsuario().equals(usuarioLogueado.getIdUsuario());
        boolean esAdmin = usuarioLogueado.getRoles().stream()
                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getNombre()));

        if (!esDueno && !esAdmin) {
            throw new RuntimeException("No tienes permiso para desactivar esta tienda.");
        }

        vendedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/perfil/logo")
    public ResponseEntity<?> subirLogo(@RequestParam("file") MultipartFile file, Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();

        Vendedor vendedor = vendedorService.buscarPorIdUsuario(usuarioLogueado.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("No tienes un perfil de vendedor activo."));

        try {
            String nombreArchivo = Objects.requireNonNull(
                    fileStorageService.guardarArchivo(file),
                    "El nombre del archivo no puede ser null");

            String urlLogo = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .pathSegment(nombreArchivo)
                    .toUriString();

            vendedor.setLogoUrl(urlLogo);
            vendedorService.actualizar(vendedor);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Logo actualizado exitosamente",
                    "logoUrl", urlLogo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar el logo en el servidor local.");
        }
    }

    @GetMapping("/pedidos")
    public ResponseEntity<List<Map<String, Object>>> listarPedidosVendedor(Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(vendedorService.listarPedidosPanel(usuarioLogueado.getIdUsuario()));
    }

    @PutMapping("/pedidos/{idPedido}/envio")
    public ResponseEntity<Map<String, Object>> actualizarEnvioPedido(
            @PathVariable Integer idPedido,
            @RequestBody Map<String, Object> payload,
            Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(
                vendedorService.actualizarEnvioPedido(usuarioLogueado.getIdUsuario(), idPedido, payload));
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<Map<String, Object>> aprobarSolicitud(@PathVariable Integer id) {
        Vendedor vendedor = vendedorService.aprobarSolicitud(id);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Solicitud aprobada correctamente",
                "idVendedor", vendedor.getIdVendedor(),
                "estado", vendedor.getEstado().name()));
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<Map<String, Object>> rechazarSolicitud(@PathVariable Integer id) {
        Vendedor vendedor = vendedorService.rechazarSolicitud(id);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Solicitud rechazada correctamente",
                "idVendedor", vendedor.getIdVendedor(),
                "estado", vendedor.getEstado().name()));
    }
}