package pe.com.punamba.backend_punamba.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.punamba.backend_punamba.entity.Atributo;
import pe.com.punamba.backend_punamba.entity.AtributoValor;
import pe.com.punamba.backend_punamba.repository.AtributoRepository;
import pe.com.punamba.backend_punamba.repository.AtributoValorRepository;

import java.util.List;
import java.util.Objects;

@Service
public class AtributoValorService {

    private final AtributoValorRepository atributoValorRepository;
    private final AtributoRepository atributoRepository;

    public AtributoValorService(AtributoValorRepository atributoValorRepository,
                                AtributoRepository atributoRepository) {
        this.atributoValorRepository = atributoValorRepository;
        this.atributoRepository = atributoRepository;
    }

    @Transactional(readOnly = true)
    public List<AtributoValor> listarPorAtributo(Integer idAtributo) {
        Integer idAtributoSeguro = Objects.requireNonNull(
                idAtributo,
                "El id del atributo no puede ser null"
        );

        return atributoValorRepository.findByAtributoIdAtributo(idAtributoSeguro);
    }

    @Transactional
    public AtributoValor crear(Integer idAtributo, String valor) {
        Integer idAtributoSeguro = Objects.requireNonNull(
                idAtributo,
                "El id del atributo no puede ser null"
        );

        if (valor == null || valor.trim().isEmpty()) {
            throw new RuntimeException("El valor es obligatorio");
        }

        Atributo atributo = atributoRepository.findById(idAtributoSeguro)
                .orElseThrow(() -> new RuntimeException("Atributo no encontrado"));

        if (atributoValorRepository.existsByAtributoIdAtributoAndValorIgnoreCase(idAtributoSeguro, valor.trim())) {
            throw new RuntimeException("Ese valor ya existe para el atributo");
        }

        AtributoValor av = new AtributoValor();
        av.setAtributo(atributo);
        av.setValor(valor.trim());

        return atributoValorRepository.save(av);
    }
}