package com.marketplace.product.service;

import com.marketplace.product.builder.ProductoBuilderImpl;
import com.marketplace.product.category.CategoriaComposite;
import com.marketplace.product.category.CategoriaLeaf;
import com.marketplace.product.dto.ProductoCreateRequest;
import com.marketplace.product.dto.ProductoResponse;
import com.marketplace.product.entity.Producto;
import com.marketplace.product.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Propósito: casos de uso para publicar y listar productos.
 * Patrón: Application Service; usa Builder para construir entidad y Composite para ruta de categoría.
 * Responsabilidad: validar reglas simples, persistir y mapear a DTOs de salida.
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional
    public ProductoResponse crear(ProductoCreateRequest request) {
        String rutaCategoria = construirRutaCategoria(request.getCategorias());
        Producto producto = new ProductoBuilderImpl()
                .conNombre(request.getNombre().trim())
                .conPrecio(request.getPrecio())
                .conDescripcion(request.getDescripcion().trim())
                .conRutaCategoria(rutaCategoria)
                .construir();
        Producto guardado = productoRepository.save(producto);
        return toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return productoRepository.findAll().stream().map(this::toResponse).toList();
    }

    private String construirRutaCategoria(List<String> categorias) {
        CategoriaComposite root = new CategoriaComposite("CATALOGO");
        categorias.stream().map(String::trim).filter(s -> !s.isBlank()).forEach(c -> root.agregar(new CategoriaLeaf(c)));
        return root.ruta();
    }

    private ProductoResponse toResponse(Producto p) {
        return new ProductoResponse(p.getId(), p.getNombre(), p.getPrecio(), p.getDescripcion(), p.getRutaCategoria(), p.getCreadoEn());
    }
}
