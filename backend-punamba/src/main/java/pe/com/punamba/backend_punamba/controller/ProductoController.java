package pe.com.punamba.backend_punamba.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.com.punamba.backend_punamba.dto.ProductoRequestDTO;
import pe.com.punamba.backend_punamba.dto.ProductoResponseDTO;
import pe.com.punamba.backend_punamba.entity.*;
import pe.com.punamba.backend_punamba.mapper.ProductoMapper;
import pe.com.punamba.backend_punamba.repository.VendedorRepository;
import pe.com.punamba.backend_punamba.service.CategoriaService;
import pe.com.punamba.backend_punamba.service.FileStorageService;
import pe.com.punamba.backend_punamba.service.ProductoService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final VendedorRepository vendedorRepository;
    private final CategoriaService categoriaService;
    private final ProductoMapper productoMapper;
    private final FileStorageService fileStorageService;

    public ProductoController(ProductoService productoService, 
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
    public ResponseEntity<List<ProductoResponseDTO>> listar() {
        List<ProductoResponseDTO> lista = productoService.listarActivos().stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtener(@PathVariable Integer id) {
        return productoService.buscarPorId(id)
                .map(p -> ResponseEntity.ok(productoMapper.toResponse(p)))
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO request, Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        
        Vendedor vendedor = vendedorRepository.findByUsuarioIdUsuario(usuarioLogueado.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("No tienes un perfil de vendedor activo para publicar productos."));
        
        Categoria categoria = categoriaService.buscarPorId(request.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("La categoría seleccionada no existe."));

        Producto nuevoProducto = productoMapper.toEntity(request);
        nuevoProducto.setCategoria(categoria);
        
        Producto guardado = productoService.guardarConRelaciones(nuevoProducto, vendedor);
        return new ResponseEntity<>(productoMapper.toResponse(guardado), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> editar(@PathVariable Integer id, @Valid @RequestBody ProductoRequestDTO request, Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        
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
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        productoService.eliminarSeguro(id, usuarioLogueado);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/imagenes")
    public ResponseEntity<?> subirImagen(@PathVariable Integer id, 
                                         @RequestParam("file") MultipartFile file, 
                                         Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        
        try {
            String nombreArchivo = fileStorageService.guardarArchivo(file);
            
            String urlImagen = "http://localhost:8090/uploads/" + nombreArchivo;
            
            ProductoImagen imagenGuardada = productoService.agregarImagenUrl(id, urlImagen, usuarioLogueado);
            
            return ResponseEntity.ok(imagenGuardada);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el archivo en el servidor.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}