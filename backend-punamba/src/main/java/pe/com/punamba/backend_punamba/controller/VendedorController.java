package pe.com.punamba.backend_punamba.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.com.punamba.backend_punamba.dto.VendedorRequestDTO;
import pe.com.punamba.backend_punamba.dto.VendedorResponseDTO;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.entity.Vendedor;
import pe.com.punamba.backend_punamba.mapper.VendedorMapper;
import pe.com.punamba.backend_punamba.service.FileStorageService;
import pe.com.punamba.backend_punamba.service.VendedorService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vendedores")
public class VendedorController {

    private final VendedorService vendedorService;
    private final VendedorMapper vendedorMapper;
    private final FileStorageService fileStorageService;

    public VendedorController(VendedorService vendedorService, 
                              VendedorMapper vendedorMapper,
                              FileStorageService fileStorageService) {
        this.vendedorService = vendedorService;
        this.vendedorMapper = vendedorMapper;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<List<VendedorResponseDTO>> listar() {
        List<VendedorResponseDTO> lista = vendedorService.listarTodos().stream()
                .map(vendedorMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendedorResponseDTO> obtener(@PathVariable Integer id) {
        return vendedorService.buscarPorId(id)
                .map(v -> ResponseEntity.ok(vendedorMapper.toResponse(v)))
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
    }

    @PostMapping
    public ResponseEntity<VendedorResponseDTO> crear(@Valid @RequestBody VendedorRequestDTO request, Authentication auth) {
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
    public ResponseEntity<VendedorResponseDTO> editar(@PathVariable Integer id, @Valid @RequestBody VendedorRequestDTO request, Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        
        Vendedor vendedorExistente = vendedorService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        boolean esDueño = vendedorExistente.getUsuario().getIdUsuario().equals(usuarioLogueado.getIdUsuario());
        boolean esAdmin = usuarioLogueado.getRoles().stream().anyMatch(r -> r.getNombre().equals("ADMIN"));
        
        if (!esDueño && !esAdmin) {
            throw new RuntimeException("No tienes permiso para editar esta tienda.");
        }

        vendedorMapper.updateEntityFromDto(request, vendedorExistente);
        Vendedor actualizado = vendedorService.actualizar(vendedorExistente);
        
        return ResponseEntity.ok(vendedorMapper.toResponse(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        vendedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/perfil/logo")
    public ResponseEntity<?> subirLogo(@RequestParam("file") MultipartFile file, Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        
        Vendedor vendedor = vendedorService.buscarPorIdUsuario(usuarioLogueado.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("No tienes un perfil de vendedor activo."));

        try {
            String nombreArchivo = fileStorageService.guardarArchivo(file);
            
            String urlLogo = "http://localhost:8090/uploads/" + nombreArchivo;
            
            vendedor.setLogoUrl(urlLogo);
            vendedorService.actualizar(vendedor);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Logo actualizado exitosamente", 
                "logoUrl", urlLogo
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar el logo en el servidor local.");
        }
    }
}