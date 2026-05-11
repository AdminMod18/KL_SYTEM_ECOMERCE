package com.marketplace.product.service;

import com.marketplace.product.builder.ProductoBuilderImpl;
import com.marketplace.product.category.CategoriaComposite;
import com.marketplace.product.category.CategoriaLeaf;
import com.marketplace.product.dto.ProductoCreateRequest;
import com.marketplace.product.dto.ProductoResponse;
import com.marketplace.product.entity.Producto;
import com.marketplace.product.integration.VendedorActivoPort;
import com.marketplace.product.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Propósito: casos de uso para publicar y listar productos.
 * Patrón: Application Service; usa Builder para construir entidad y Composite para ruta de categoría.
 * Responsabilidad: validar reglas simples, persistir y mapear a DTOs de salida.
 */
@Service
public class ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    /** Máximo de entradas de imagen (archivo comprimido y/o URL) por producto. */
    private static final int MAX_IMAGENES = 12;

    /** Tamaño máximo por cadena (URL o data URL), suficiente para JPEG comprimido en demo. */
    private static final int MAX_CARACTERES_POR_IMAGEN = 250_000;

    private final ProductoRepository productoRepository;
    private final VendedorActivoPort vendedorActivoPort;

    public ProductoService(ProductoRepository productoRepository, VendedorActivoPort vendedorActivoPort) {
        this.productoRepository = productoRepository;
        this.vendedorActivoPort = vendedorActivoPort;
    }

    @Transactional
    public ProductoResponse crear(ProductoCreateRequest request) {
        validarImagenesUrls(request.getImagenesUrls());
        vendedorActivoPort.assertVendedorEnEstadoActiva(request.getVendedorSolicitudId());
        String rutaCategoria = construirRutaCategoria(request.getCategorias());
        Producto producto = new ProductoBuilderImpl()
                .conNombre(request.getNombre().trim())
                .conPrecio(request.getPrecio())
                .conDescripcion(request.getDescripcion().trim())
                .conRutaCategoria(rutaCategoria)
                .construir();
        producto.setVendedorSolicitudId(request.getVendedorSolicitudId());
        producto.setSubcategoria(blancoANulo(request.getSubcategoria()));
        producto.setMarca(blancoANulo(request.getMarca()));
        producto.setOriginalidad(request.getOriginalidad());
        producto.setColor(blancoANulo(request.getColor()));
        producto.setTamano(blancoANulo(request.getTamano()));
        producto.setPesoGramos(request.getPesoGramos());
        producto.setTalla(blancoANulo(request.getTalla()));
        producto.setCondicion(request.getCondicion());
        producto.setCantidadStock(request.getCantidadStock() != null ? request.getCantidadStock() : 1);
        producto.setImagenesUrls(imagenesListaACadena(request.getImagenesUrls()));
        Producto guardado = productoRepository.save(producto);
        return toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        List<ProductoResponse> lista = productoRepository.findAll().stream().map(this::toResponse).toList();
        log.info("Listado de productos: {} item(s)", lista.size());
        return lista;
    }

    private String construirRutaCategoria(List<String> categorias) {
        CategoriaComposite root = new CategoriaComposite("CATALOGO");
        categorias.stream().map(String::trim).filter(s -> !s.isBlank()).forEach(c -> root.agregar(new CategoriaLeaf(c)));
        return root.ruta();
    }

    private ProductoResponse toResponse(Producto p) {
        return new ProductoResponse(
                p.getId(),
                p.getVendedorSolicitudId(),
                p.getNombre(),
                p.getPrecio(),
                p.getDescripcion(),
                p.getRutaCategoria(),
                p.getSubcategoria(),
                p.getMarca(),
                p.getOriginalidad(),
                p.getColor(),
                p.getTamano(),
                p.getPesoGramos(),
                p.getTalla(),
                p.getCondicion(),
                p.getCantidadStock(),
                p.getImagenesUrls(),
                p.getCreadoEn());
    }

    private static String blancoANulo(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }

    private static String imagenesListaACadena(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return null;
        }
        /** No usar coma: las data URLs incluyen "," tras "base64," y romperían el split en el cliente. */
        return urls.stream()
                .filter(u -> u != null && !u.isBlank())
                .map(String::trim)
                .collect(Collectors.joining("\n"));
    }

    private static void validarImagenesUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        if (urls.size() > MAX_IMAGENES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Máximo " + MAX_IMAGENES + " imágenes (varias tomas / URLs).");
        }
        for (int i = 0; i < urls.size(); i++) {
            String raw = urls.get(i);
            if (raw == null || raw.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "imagenesUrls[" + i + "]: valor vacío.");
            }
            String u = raw.trim();
            if (u.length() > MAX_CARACTERES_POR_IMAGEN) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "imagenesUrls[" + i + "]: supera " + MAX_CARACTERES_POR_IMAGEN + " caracteres; comprime más o usa URL.");
            }
        }
    }
}
