package pe.com.punamba.backend_punamba.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.dto.AtributoValorRequestDTO;
import pe.com.punamba.backend_punamba.entity.AtributoValor;
import pe.com.punamba.backend_punamba.service.AtributoValorService;

import java.util.List;

@RestController
@RequestMapping("/api/atributo-valores")
public class AtributoValorController {

    private final AtributoValorService atributoValorService;

    public AtributoValorController(AtributoValorService atributoValorService) {
        this.atributoValorService = atributoValorService;
    }

    @GetMapping("/atributo/{idAtributo}")
    public ResponseEntity<List<AtributoValor>> listarPorAtributo(@PathVariable Integer idAtributo) {
        return ResponseEntity.ok(atributoValorService.listarPorAtributo(idAtributo));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody AtributoValorRequestDTO request) {
        try {
            AtributoValor valor = atributoValorService.crear(request.getIdAtributo(), request.getValor());
            return new ResponseEntity<>(valor, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}