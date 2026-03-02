package pe.com.punamba.backend_punamba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Atributo;
import pe.com.punamba.backend_punamba.entity.AtributoValor;
import pe.com.punamba.backend_punamba.repository.AtributoRepository;
import pe.com.punamba.backend_punamba.repository.AtributoValorRepository;

import java.util.List;

@Service
public class AtributoService {

    @Autowired
    private AtributoRepository atributoRepository;

    @Autowired
    private AtributoValorRepository atributoValorRepository;

    @Transactional(readOnly = true)
    public List<Atributo> listarTodos() {
        return atributoRepository.findAll();
    }

    @Transactional
    public Atributo guardarAtributo(Atributo atributo) {
        return atributoRepository.save(atributo);
    }

    @Transactional
    public AtributoValor agregarValor(Integer idAtributo, AtributoValor valor) {
        Atributo atributo = atributoRepository.findById(idAtributo)
                .orElseThrow(() -> new RuntimeException("Atributo no encontrado"));
        
        valor.setAtributo(atributo);
        return atributoValorRepository.save(valor);
    }
}