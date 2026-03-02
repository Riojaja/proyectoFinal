package pe.com.punamba.backend_punamba.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.punamba.backend_punamba.entity.Ubigeo;
import pe.com.punamba.backend_punamba.service.UbigeoService;

import java.util.List;

@RestController
@RequestMapping("/api/ubigeos")
@CrossOrigin(origins = "*")
public class UbigeoController {

    @Autowired
    private UbigeoService ubigeoService;

    @GetMapping("/departamentos")
    public ResponseEntity<List<String>> listarDepartamentos() {
        return ResponseEntity.ok(ubigeoService.obtenerDepartamentos());
    }

    @GetMapping("/provincias/{departamento}")
    public ResponseEntity<List<String>> listarProvincias(@PathVariable String departamento) {
        return ResponseEntity.ok(ubigeoService.obtenerProvincias(departamento));
    }

    @GetMapping("/distritos/{departamento}/{provincia}")
    public ResponseEntity<List<Ubigeo>> listarDistritos(@PathVariable String departamento, @PathVariable String provincia) {
        return ResponseEntity.ok(ubigeoService.obtenerDistritos(departamento, provincia));
    }

    @GetMapping
    public ResponseEntity<List<Ubigeo>> listarTodos() {
        return ResponseEntity.ok(ubigeoService.listarTodos());
    }

    @GetMapping("/{codigoUbigeo}")
    public ResponseEntity<Ubigeo> obtenerPorId(@PathVariable String codigoUbigeo) {
        return ubigeoService.buscarPorId(codigoUbigeo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Ubigeo> crear(@RequestBody Ubigeo ubigeo) {
        return new ResponseEntity<>(ubigeoService.guardar(ubigeo), HttpStatus.CREATED);
    }

    @PutMapping("/{codigoUbigeo}")
    public ResponseEntity<Ubigeo> editar(@PathVariable String codigoUbigeo, @RequestBody Ubigeo detalles) {
        return ubigeoService.buscarPorId(codigoUbigeo).map(u -> {
            u.setDepartamento(detalles.getDepartamento());
            u.setProvincia(detalles.getProvincia());
            u.setDistrito(detalles.getDistrito());
            return ResponseEntity.ok(ubigeoService.guardar(u));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{codigoUbigeo}")
    public ResponseEntity<Void> eliminar(@PathVariable String codigoUbigeo) {
        ubigeoService.eliminar(codigoUbigeo);
        return ResponseEntity.noContent().build();
    }
}