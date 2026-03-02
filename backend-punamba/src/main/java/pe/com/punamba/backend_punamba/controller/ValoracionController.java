package pe.com.punamba.backend_punamba.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.ValoracionRequestDTO;
import pe.com.punamba.backend_punamba.dto.ValoracionResponseDTO;
import pe.com.punamba.backend_punamba.entity.ProductoVariante;
import pe.com.punamba.backend_punamba.entity.Usuario;
import pe.com.punamba.backend_punamba.entity.Valoracion;
import pe.com.punamba.backend_punamba.mapper.ValoracionMapper;
import pe.com.punamba.backend_punamba.service.ValoracionService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/valoraciones")
@CrossOrigin(origins = "*")
public class ValoracionController {

    @Autowired private ValoracionService valoracionService;
    @Autowired private ValoracionMapper valoracionMapper;

    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<List<ValoracionResponseDTO>> listarPorProducto(@PathVariable Integer idProducto) {
        return ResponseEntity.ok(valoracionService.listarPorProducto(idProducto).stream()
                .map(valoracionMapper::toResponse).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody ValoracionRequestDTO request, Authentication auth) {
        Usuario usuarioLogueado = (Usuario) auth.getPrincipal();
        try {
            Valoracion valoracion = new Valoracion();
            valoracion.setUsuario(usuarioLogueado);
            
            ProductoVariante variante = new ProductoVariante();
            variante.setIdVariante(request.getIdVariante());
            valoracion.setVariante(variante);
            
            valoracion.setCalificacion(request.getCalificacion());
            valoracion.setTitulo(request.getTitulo());
            valoracion.setComentario(request.getComentario());
            valoracion.setImagenes(request.getImagenes());
            valoracion.setVerificada(request.getIdOrdenDetalle() != null); // Simplificación de verificación

            return new ResponseEntity<>(valoracionMapper.toResponse(valoracionService.guardar(valoracion)), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}