package com.marketplace.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Propósito: entidad persistida de producto publicado por vendedor.
 * Patrón: producto final construido vía Builder Pattern.
 * Responsabilidad: almacenar datos de catálogo con ruta de categoría compuesta.
 */
@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal precio;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(nullable = false, length = 300)
    private String rutaCategoria;

    @Column(nullable = false)
    private Instant creadoEn;

    @PrePersist
    void onCreate() {
        if (creadoEn == null) {
            creadoEn = Instant.now();
        }
    }
}
