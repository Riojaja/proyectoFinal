package pe.com.punamba.backend_punamba.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.entity.Atributo;
import pe.com.punamba.backend_punamba.entity.AtributoValor;
import pe.com.punamba.backend_punamba.service.AtributoService;

import java.util.List;

@RestController
@RequestMapping("/api/atributos")
public class AtributoController {

    private final AtributoService atributoService;

    public AtributoController(AtributoService atributoService) {
        this.atributoService = atributoService;
    }

    @GetMapping
    public ResponseEntity<List<Atributo>> listar() {
        return ResponseEntity.ok(atributoService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<Atributo> crearAtributo(@RequestBody Atributo atributo) {
        return ResponseEntity.ok(atributoService.guardarAtributo(atributo));
    }

    @PostMapping("/{id}/valores")
    public ResponseEntity<AtributoValor> agregarValor(@PathVariable Integer id, @RequestBody AtributoValor valor) {
        return ResponseEntity.ok(atributoService.agregarValor(id, valor));
    }
}