package com.marketplace.solicitud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Calificación de un comprador al vendedor asociado a una solicitud activa (caso estudio §6).
 */
@Entity
@Table(name = "calificaciones_vendedor")
@Getter
@Setter
@NoArgsConstructor
public class CalificacionVendedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    /** Escala 1–10 según caso estudio. */
    @Column(nullable = false)
    private int valor;

    @Column(length = 500)
    private String comentario;

    @Column(name = "referencia_orden", length = 64)
    private String referenciaOrden;

    @Column(nullable = false)
    private Instant creadoEn;

    @PrePersist
    void onCreate() {
        if (creadoEn == null) {
            creadoEn = Instant.now();
        }
    }
}
