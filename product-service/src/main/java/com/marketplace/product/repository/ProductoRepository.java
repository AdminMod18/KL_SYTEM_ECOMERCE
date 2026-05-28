package com.marketplace.product.repository;

import com.marketplace.product.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Propósito: repositorio JPA para operaciones CRUD de productos.
 * Patrón: Repository.
 * Responsabilidad: abstraer acceso a base de datos del servicio de aplicación.
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByVendedorSolicitudIdOrderByCreadoEnDesc(Long vendedorSolicitudId);
}
