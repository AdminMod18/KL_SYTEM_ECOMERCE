package com.marketplace.product.entity;

import com.marketplace.product.model.ProductoInteraccionTipo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "producto_interacciones")
@Getter
@Setter
@NoArgsConstructor
public class ProductoInteraccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ProductoInteraccionTipo tipo;

    @Column(nullable = false, length = 2000)
    private String contenido;

    @Column(length = 120)
    private String autorNombre;

    /** Respuesta del vendedor (solo aplica a {@link ProductoInteraccionTipo#PREGUNTA}). */
    @Column(length = 2000)
    private String respuesta;

    @Column(nullable = false)
    private Instant creadoEn;

    @PrePersist
    void onCreate() {
        if (creadoEn == null) {
            creadoEn = Instant.now();
        }
    }
}
