package pe.com.punamba.backend_punamba.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.MetodoPagoDTO;
//import pe.com.punamba.backend_punamba.dto.TransaccionResponseDTO;
import pe.com.punamba.backend_punamba.entity.Transaccion;
import pe.com.punamba.backend_punamba.mapper.PagoMapper;
import pe.com.punamba.backend_punamba.service.PagoService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
public class PagoController {

    @Autowired private PagoService pagoService;
    @Autowired private PagoMapper pagoMapper;

    @GetMapping("/metodos")
    public ResponseEntity<List<MetodoPagoDTO>> listarMetodos() {
        return ResponseEntity.ok(pagoService.listarMetodosActivos().stream()
                .map(pagoMapper::toMetodoDTO).collect(Collectors.toList()));
    }

    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarPago(@RequestBody Map<String, String> payload) {
        try {
            Integer idOrden = Integer.parseInt(payload.get("idOrden"));
            Integer idMetodo = Integer.parseInt(payload.get("idMetodo"));
            String nroOperacion = payload.get("nroOperacion");

            Transaccion nuevaTransaccion = pagoService.procesarPago(idOrden, idMetodo, nroOperacion);
            return new ResponseEntity<>(pagoMapper.toTransaccionDTO(nuevaTransaccion), HttpStatus.CREATED);

        } catch (NumberFormatException e) {
            return new ResponseEntity<>("Los IDs de orden y método deben ser números válidos", HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}