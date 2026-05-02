package com.marketplace.product.service;

import com.marketplace.product.dto.ProductoInteraccionCreateRequest;
import com.marketplace.product.dto.ProductoInteraccionRespuestaRequest;
import com.marketplace.product.dto.ProductoInteraccionResponse;
import com.marketplace.product.entity.ProductoInteraccion;
import com.marketplace.product.model.ProductoInteraccionTipo;
import com.marketplace.product.repository.ProductoInteraccionRepository;
import com.marketplace.product.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductoInteraccionService {

    private final ProductoRepository productoRepository;
    private final ProductoInteraccionRepository interaccionRepository;

    public ProductoInteraccionService(
            ProductoRepository productoRepository, ProductoInteraccionRepository interaccionRepository) {
        this.productoRepository = productoRepository;
        this.interaccionRepository = interaccionRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoInteraccionResponse> listarPorProducto(Long productoId) {
        assertProductoExiste(productoId);
        return interaccionRepository.findByProductoIdOrderByCreadoEnDesc(productoId).stream()
                .map(ProductoInteraccionService::toResponse)
                .toList();
    }

    @Transactional
    public ProductoInteraccionResponse crear(Long productoId, ProductoInteraccionCreateRequest request) {
        assertProductoExiste(productoId);
        ProductoInteraccion e = new ProductoInteraccion();
        e.setProductoId(productoId);
        e.setTipo(request.getTipo());
        e.setContenido(request.getContenido().trim());
        if (request.getAutorNombre() != null && !request.getAutorNombre().isBlank()) {
            e.setAutorNombre(request.getAutorNombre().trim());
        }
        return toResponse(interaccionRepository.save(e));
    }

    @Transactional
    public ProductoInteraccionResponse responder(
            Long productoId, Long interaccionId, ProductoInteraccionRespuestaRequest request) {
        assertProductoExiste(productoId);
        ProductoInteraccion e =
                interaccionRepository
                        .findById(interaccionId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interacción no encontrada."));
        if (!e.getProductoId().equals(productoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Interacción no pertenece al producto.");
        }
        if (e.getTipo() != ProductoInteraccionTipo.PREGUNTA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo las preguntas admiten respuesta del vendedor.");
        }
        e.setRespuesta(request.getRespuesta().trim());
        return toResponse(interaccionRepository.save(e));
    }

    private void assertProductoExiste(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado.");
        }
    }

    private static ProductoInteraccionResponse toResponse(ProductoInteraccion e) {
        return new ProductoInteraccionResponse(
                e.getId(),
                e.getProductoId(),
                e.getTipo(),
                e.getContenido(),
                e.getAutorNombre(),
                e.getRespuesta(),
                e.getCreadoEn());
    }
}
