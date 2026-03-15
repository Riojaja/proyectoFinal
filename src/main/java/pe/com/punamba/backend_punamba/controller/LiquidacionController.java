package pe.com.punamba.backend_punamba.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.com.punamba.backend_punamba.dto.LiquidacionResponseDTO;
import pe.com.punamba.backend_punamba.entity.Liquidacion;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.entity.Vendedor;
import pe.com.punamba.backend_punamba.mapper.LiquidacionMapper;
import pe.com.punamba.backend_punamba.service.FileStorageService;
import pe.com.punamba.backend_punamba.service.LiquidacionService;
import pe.com.punamba.backend_punamba.service.VendedorService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/liquidaciones")
public class LiquidacionController {

    @Autowired
    private LiquidacionService liquidacionService;
    @Autowired
    private VendedorService vendedorService;
    @Autowired
    private LiquidacionMapper liquidacionMapper;
    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/mis-liquidaciones")
    public ResponseEntity<List<LiquidacionResponseDTO>> listarMisLiquidaciones(Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        Vendedor vendedor = vendedorService.buscarPorIdUsuario(usuarioLogueado.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("No eres vendedor"));

        return ResponseEntity.ok(
                liquidacionService.listarPorVendedor(vendedor.getIdVendedor())
                        .stream()
                        .map(liquidacionMapper::toResponse)
                        .collect(Collectors.toList()));
    }

    @GetMapping
    public ResponseEntity<List<LiquidacionResponseDTO>> listarTodas() {
        return ResponseEntity.ok(
                liquidacionService.listarTodas()
                        .stream()
                        .map(liquidacionMapper::toResponse)
                        .collect(Collectors.toList()));
    }

    @PostMapping("/vendedor/{idVendedor}/generar")
    public ResponseEntity<?> generar(@PathVariable Integer idVendedor, @RequestBody Liquidacion liquidacion) {
        try {
            return new ResponseEntity<>(
                    liquidacionMapper.toResponse(liquidacionService.generarLiquidacion(idVendedor, liquidacion)),
                    HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/comprobante")
    public ResponseEntity<?> subirComprobante(@RequestParam("file") MultipartFile file) {
        try {
            String nombreArchivo = Objects.requireNonNull(
                    fileStorageService.guardarArchivo(file),
                    "El nombre del archivo no puede ser null");

            String urlArchivo = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .pathSegment(nombreArchivo)
                    .toUriString();

            return ResponseEntity.ok(Map.of(
                    "fileName", nombreArchivo,
                    "url", urlArchivo));
        } catch (IOException e) {
            return new ResponseEntity<>("No se pudo subir el comprobante", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<?> registrarPago(@PathVariable Integer id, @RequestBody Map<String, String> payload) {
        String comprobanteUrl = payload.get("comprobanteUrl");
        if (comprobanteUrl == null || comprobanteUrl.trim().isEmpty()) {
            return new ResponseEntity<>("La URL del comprobante es obligatoria", HttpStatus.BAD_REQUEST);
        }

        try {
            return ResponseEntity.ok(
                    liquidacionMapper.toResponse(liquidacionService.registrarPago(id, comprobanteUrl)));
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}