package com.marketplace.product.config;

import com.marketplace.product.entity.Producto;
import com.marketplace.product.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Carga un producto demo en memoria (perfil {@code demo}) para pruebas E2E sin solicitud-service.
 */
@Component
@Profile("demo")
public class CatalogoDemoDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogoDemoDataLoader.class);

    private final ProductoRepository productoRepository;

    public CatalogoDemoDataLoader(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (productoRepository.count() > 0) {
            return;
        }
        Producto p = new Producto();
        p.setNombre("Artículo demo E2E");
        p.setPrecio(new BigDecimal("10.00"));
        p.setDescripcion("Producto sembrado para validación de compra (perfil demo).");
        p.setRutaCategoria("CATALOGO/Demo");
        p.setVendedorSolicitudId(1L);
        productoRepository.save(p);
        log.info("Catálogo demo: producto id={} precio={} (perfil demo)", p.getId(), p.getPrecio());
    }
}
