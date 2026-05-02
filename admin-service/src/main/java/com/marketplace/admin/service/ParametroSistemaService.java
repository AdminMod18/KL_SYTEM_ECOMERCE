package com.marketplace.admin.service;

import com.marketplace.admin.dto.ParametroResponse;
import com.marketplace.admin.dto.ParametroUpdateRequest;
import com.marketplace.admin.entity.ParametroSistema;
import com.marketplace.admin.repository.ParametroSistemaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ParametroSistemaService {

    private final ParametroSistemaRepository repository;

    public ParametroSistemaService(ParametroSistemaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ParametroResponse> listar() {
        return repository.findAll().stream()
                .sorted((a, b) -> a.getClave().compareToIgnoreCase(b.getClave()))
                .map(p -> new ParametroResponse(p.getClave(), p.getValor(), p.getActualizadoEn()))
                .toList();
    }

    @Transactional
    public ParametroResponse actualizar(String clave, ParametroUpdateRequest request) {
        ParametroSistema p = repository
                .findByClave(clave.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parametro no encontrado"));
        p.setValor(request.getValor().trim());
        ParametroSistema g = repository.save(p);
        return new ParametroResponse(g.getClave(), g.getValor(), g.getActualizadoEn());
    }
}
