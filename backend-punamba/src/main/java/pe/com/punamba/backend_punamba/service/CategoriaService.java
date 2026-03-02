package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Categoria;
import pe.com.punamba.backend_punamba.repository.CategoriaRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<Categoria> listarPrincipales() {
        return categoriaRepository.findByCategoriaPadreIsNullAndEstadoTrue();
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Categoria> buscarPorId(Integer id) {
        return categoriaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarSubcategorias(Integer idPadre) {
        return categoriaRepository.findByCategoriaPadreIdCategoria(idPadre);
    }

    @Transactional
    public Categoria guardar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void eliminar(Integer id) {
        categoriaRepository.findById(id).ifPresent(c -> {
            c.setEstado(false);
            categoriaRepository.save(c);
        });
    }

    public boolean existeNombre(String nombre) {
        return categoriaRepository.existsByNombre(nombre);
    }
}