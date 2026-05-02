package com.marketplace.product.entity;

import com.marketplace.product.model.CondicionProductoCatalogo;
import com.marketplace.product.model.OriginalidadProducto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;
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

    @Column(length = 120)
    private String subcategoria;

    @Column(length = 120)
    private String marca;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private OriginalidadProducto originalidad;

    @Column(length = 80)
    private String color;

    @Column(length = 80)
    private String tamano;

    @Column(name = "peso_gramos")
    private Integer pesoGramos;

    @Column(length = 40)
    private String talla;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private CondicionProductoCatalogo condicion;

    @ColumnDefault("0")
    @Column(name = "cantidad_stock", nullable = false)
    private Integer cantidadStock = 0;

    /** URLs separadas por coma (demo sin almacenamiento de binarios). */
    @Column(length = 2000)
    private String imagenesUrls;

    /** Id de la solicitud de vendedor en solicitud-service (vendedor habilitado). */
    @Column(name = "vendedor_solicitud_id", nullable = false)
    private Long vendedorSolicitudId;

    @Column(nullable = false)
    private Instant creadoEn;

    @PrePersist
    void onCreate() {
        if (creadoEn == null) {
            creadoEn = Instant.now();
        }
    }
}
