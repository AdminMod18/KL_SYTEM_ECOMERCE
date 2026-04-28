package com.marketplace.order.repository;

import com.marketplace.order.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Propósito: persistencia de órdenes y sus líneas (cascade desde el agregado).
 * Patrón: Repository (Spring Data JPA).
 * Responsabilidad: operaciones CRUD sobre {@link Orden}.
 */
public interface OrdenRepository extends JpaRepository<Orden, Long> {
}
