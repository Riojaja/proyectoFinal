package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Ubigeo;
import pe.com.punamba.backend_punamba.repository.UbigeoRepository;

import java.util.List;
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
        return ubigeoRepository.findDistinctProvinciasByDepartamento(departamento);
    }

    @Transactional(readOnly = true)
    public List<Ubigeo> obtenerDistritos(String departamento, String provincia) {
        return ubigeoRepository.findDistritosByDepartamentoAndProvincia(departamento, provincia);
    }

    @Transactional(readOnly = true)
    public List<Ubigeo> listarTodos() {
        return ubigeoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Ubigeo> buscarPorId(String codigoUbigeo) {
        return ubigeoRepository.findById(codigoUbigeo);
    }

    @Transactional
    public Ubigeo guardar(Ubigeo ubigeo) {
        return ubigeoRepository.save(ubigeo);
    }

    @Transactional
    public void eliminar(String codigoUbigeo) {
        ubigeoRepository.deleteById(codigoUbigeo);
    }
}