package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Ubigeo;
import pe.com.punamba.backend_punamba.repository.UbigeoRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UbigeoService {

    @Autowired
    private UbigeoRepository ubigeoRepository;

    @Transactional(readOnly = true)
    public List<String> obtenerDepartamentos() {
        return ubigeoRepository.findDistinctDepartamentos();
    }

    @Transactional(readOnly = true)
    public List<String> obtenerProvincias(String departamento) {
        String departamentoSeguro = Objects.requireNonNull(departamento, "El departamento no puede ser null");
        return ubigeoRepository.findDistinctProvinciasByDepartamento(departamentoSeguro);
    }

    @Transactional(readOnly = true)
    public List<Ubigeo> obtenerDistritos(String departamento, String provincia) {
        String departamentoSeguro = Objects.requireNonNull(departamento, "El departamento no puede ser null");
        String provinciaSegura = Objects.requireNonNull(provincia, "La provincia no puede ser null");
        return ubigeoRepository.findDistritosByDepartamentoAndProvincia(departamentoSeguro, provinciaSegura);
    }

    @Transactional(readOnly = true)
    public List<Ubigeo> listarTodos() {
        return ubigeoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Ubigeo> buscarPorId(String codigoUbigeo) {
        String codigoUbigeoSeguro = Objects.requireNonNull(codigoUbigeo, "El código ubigeo no puede ser null");
        return ubigeoRepository.findById(codigoUbigeoSeguro);
    }

    @Transactional
    public Ubigeo guardar(Ubigeo ubigeo) {
        Ubigeo ubigeoSeguro = Objects.requireNonNull(ubigeo, "El ubigeo no puede ser null");
        return ubigeoRepository.save(ubigeoSeguro);
    }

    @Transactional
    public void eliminar(String codigoUbigeo) {
        String codigoUbigeoSeguro = Objects.requireNonNull(codigoUbigeo, "El código ubigeo no puede ser null");
        ubigeoRepository.deleteById(codigoUbigeoSeguro);
    }
}