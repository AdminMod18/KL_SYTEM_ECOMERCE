package com.marketplace.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Propósito: línea de detalle de una orden (SKU, cantidad y precio unitario acordado).
 * Patrón: entidad hijo del agregado Orden.
 * Responsabilidad: persistir cantidades y precios usados en el cálculo del total.
 */
@Entity
@Table(name = "orden_lineas")
@Getter
@Setter
@NoArgsConstructor
public class OrdenLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_id")
    private Orden orden;

    @Column(nullable = false, length = 64)
    private String sku;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario;
}
