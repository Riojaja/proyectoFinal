package pe.com.punamba.backend_punamba.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.com.punamba.backend_punamba.dto.ProductoRequestDTO;
import pe.com.punamba.backend_punamba.dto.ProductoResponseDTO;
import pe.com.punamba.backend_punamba.entity.Categoria;
import pe.com.punamba.backend_punamba.entity.Producto;
import pe.com.punamba.backend_punamba.entity.ProductoImagen;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.entity.Vendedor;
import pe.com.punamba.backend_punamba.mapper.ProductoMapper;
import pe.com.punamba.backend_punamba.repository.VendedorRepository;
import pe.com.punamba.backend_punamba.service.CategoriaService;
import pe.com.punamba.backend_punamba.service.FileStorageService;
import pe.com.punamba.backend_punamba.service.ProductoService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

        private final ProductoService productoService;
        private final VendedorRepository vendedorRepository;
        private final CategoriaService categoriaService;
        private final ProductoMapper productoMapper;
        private final FileStorageService fileStorageService;

        public ProductoController(
                        ProductoService productoService,
                        VendedorRepository vendedorRepository,
                        CategoriaService categoriaService,
                        ProductoMapper productoMapper,
                        FileStorageService fileStorageService) {
                this.productoService = productoService;
                this.vendedorRepository = vendedorRepository;
                this.categoriaService = categoriaService;
                this.productoMapper = productoMapper;
                this.fileStorageService = fileStorageService;
        }

        @GetMapping
        public ResponseEntity<List<ProductoResponseDTO>> listar(
                        @RequestParam(required = false) Integer categoria,
                        @RequestParam(required = false) String tipo,
                        @RequestParam(required = false, defaultValue = "12") Integer limit) {
                List<ProductoResponseDTO> lista = productoService.listarHome(categoria, tipo, limit).stream()
                                .map(productoMapper::toResponse)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(lista);
        }

        @GetMapping("/ofertas")
        public ResponseEntity<List<ProductoResponseDTO>> listarOfertas(
                        @RequestParam(required = false, defaultValue = "12") Integer limit) {
                List<ProductoResponseDTO> lista = productoService.listarOfertas(limit).stream()
                                .map(productoMapper::toResponse)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(lista);
        }

        @GetMapping("/nuevos")
        public ResponseEntity<List<ProductoResponseDTO>> listarNuevos(
                        @RequestParam(required = false, defaultValue = "12") Integer limit) {
                List<ProductoResponseDTO> lista = productoService.listarNuevos(limit).stream()
                                .map(productoMapper::toResponse)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(lista);
        }

        @GetMapping("/mas-vendidos")
        public ResponseEntity<List<ProductoResponseDTO>> listarMasVendidos(
                        @RequestParam(required = false, defaultValue = "12") Integer limit) {
                List<ProductoResponseDTO> lista = productoService.listarMasVendidos(limit).stream()
                                .map(productoMapper::toResponse)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(lista);
        }

        @GetMapping("/mis")
        public ResponseEntity<List<ProductoResponseDTO>> listarMisProductos(Authentication auth) {
                Usuario usuarioLogueado = obtenerUsuarioAutenticado(auth);

                Vendedor vendedor = vendedorRepository.findByUsuarioIdUsuario(usuarioLogueado.getIdUsuario())
                                .orElseThrow(() -> new RuntimeException("No tienes un perfil de vendedor activo."));

                List<ProductoResponseDTO> lista = productoService.listarPorVendedor(vendedor.getIdVendedor()).stream()
                                .map(productoMapper::toResponse)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(lista);
        }

        @GetMapping("/{id}")
        public ResponseEntity<ProductoResponseDTO> obtener(@PathVariable Integer id) {
                return productoService.buscarPorId(id)
                                .map(productoMapper::toResponse)
                                .map(ResponseEntity::ok)
                                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        }

        @GetMapping("/{id}/imagenes")
        public ResponseEntity<List<ProductoImagen>> listarImagenes(@PathVariable Integer id) {
                return ResponseEntity.ok(productoService.listarImagenesPorProducto(id));
        }

        @PostMapping
        public ResponseEntity<ProductoResponseDTO> crear(
                        @Valid @RequestBody ProductoRequestDTO request,
                        Authentication auth) {
                Usuario usuarioLogueado = obtenerUsuarioAutenticado(auth);

                Vendedor vendedor = vendedorRepository.findByUsuarioIdUsuario(usuarioLogueado.getIdUsuario())
                                .orElseThrow(() -> new RuntimeException(
                                                "No tienes un perfil de vendedor activo para publicar productos."));

                Categoria categoria = categoriaService.buscarPorId(request.getIdCategoria())
                                .orElseThrow(() -> new RuntimeException("La categoría seleccionada no existe."));

                Producto nuevoProducto = productoMapper.toEntity(request);
                nuevoProducto.setCategoria(categoria);

                Producto guardado = productoService.guardarConRelaciones(nuevoProducto, vendedor);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(productoMapper.toResponse(guardado));
        }

        @PutMapping("/{id}")
        public ResponseEntity<ProductoResponseDTO> editar(
                        @PathVariable Integer id,
                        @Valid @RequestBody ProductoRequestDTO request,
                        Authentication auth) {
                Usuario usuarioLogueado = obtenerUsuarioAutenticado(auth);

                Categoria categoria = categoriaService.buscarPorId(request.getIdCategoria())
                                .orElseThrow(() -> new RuntimeException("La categoría seleccionada no existe."));

                Producto productoExistente = productoService.buscarPorId(id)
                                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                productoMapper.updateEntityFromDto(request, productoExistente);
                productoExistente.setCategoria(categoria);

                Producto actualizado = productoService.actualizar(id, productoExistente, usuarioLogueado);

                return ResponseEntity.ok(productoMapper.toResponse(actualizado));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> eliminar(@PathVariable Integer id, Authentication auth) {
                Usuario usuarioLogueado = obtenerUsuarioAutenticado(auth);
                productoService.eliminarSeguro(id, usuarioLogueado);
                return ResponseEntity.noContent().build();
        }

        @PostMapping("/{id}/imagenes")
        public ResponseEntity<?> subirImagen(
                        @PathVariable Integer id,
                        @RequestParam("file") MultipartFile file,
                        Authentication auth) {
                Usuario usuarioLogueado = obtenerUsuarioAutenticado(auth);

                try {
                        String nombreArchivo = Objects.requireNonNull(
                                        fileStorageService.guardarArchivo(file),
                                        "El nombre del archivo no puede ser null");

                        String urlImagen = ServletUriComponentsBuilder.fromCurrentContextPath()
                                        .path("/uploads/")
                                        .pathSegment(nombreArchivo)
                                        .toUriString();

                        ProductoImagen imagenGuardada = productoService.agregarImagenUrl(id, urlImagen,
                                        usuarioLogueado);
                        return ResponseEntity.ok(imagenGuardada);

                } catch (IOException e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error al procesar el archivo en el servidor.");
                } catch (RuntimeException e) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(e.getMessage());
                }
        }

        private Usuario obtenerUsuarioAutenticado(Authentication auth) {
                if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
                        throw new RuntimeException("Usuario no autenticado");
                }
                return usuario;
        }
}