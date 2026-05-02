package com.marketplace.product.repository;

import com.marketplace.product.entity.ProductoInteraccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoInteraccionRepository extends JpaRepository<ProductoInteraccion, Long> {

    List<ProductoInteraccion> findByProductoIdOrderByCreadoEnDesc(Long productoId);
}
