package com.marketplace.analytics.entity;

import com.marketplace.analytics.model.TipoEventoMetrica;
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

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Propósito: registro atómico de un hecho de negocio para agregación posterior (KPIs).
 * Patrón: entidad de hechos (event store simplificado).
 * Responsabilidad: almacenar tipo, referencia, valor opcional y marca temporal.
 */
@Entity
@Table(name = "eventos_metrica")
@Getter
@Setter
@NoArgsConstructor
public class EventoMetrica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoEventoMetrica tipo;

    @Column(nullable = false, length = 120)
    private String referencia;

    @Column(precision = 19, scale = 4)
    private BigDecimal valorMonetario;

    @Column(nullable = false)
    private Instant ocurridoEn;

    @PrePersist
    void onCreate() {
        if (ocurridoEn == null) {
            ocurridoEn = Instant.now();
        }
    }
}
